layui.use(['layer', 'element'], function () {
    const layer = layui.layer;
    const element = layui.element;
    const $ = layui.$;

    // 接口地址
    const API_BASE = '/article';

    // 关闭 mock
    const USE_MOCK = false;

    const state = {
        articleId: getQueryParam('id'),
        loadingIndex: null
    };
    // 获取文章ID
    const id = new URLSearchParams(window.location.search).get("id");

    console.log("当前文章ID:", id);
    init();

    function init() {
        bindBackToTop();
        bindNavigation();
        initMarkdown()

        if (!state.articleId) {
            layer.msg('缺少文章 ID');
            renderEmptyPage('未找到文章');
            return;
        }

        loadArticle(state.articleId);
    }
    //markdown初始化
    function initMarkdown() {
        if (!window.marked) return;

        //  自定义 renderer：代码块交给 hljs，表格加 wrapper
        const renderer = new marked.Renderer();

        // ── 代码高亮 ──
        // marked v9+ 的 renderer.code 接收单个 token 对象 {text, lang, escaped}
        // marked v4-v8 接收 (code, lang) 两个参数
        renderer.code = function (token) {
            const codeText = (token && typeof token === 'object' && 'text' in token) ? token.text : String(token);
            const codeLang = (token && typeof token === 'object' && 'lang' in token) ? token.lang : arguments[1];

            if (window.hljs) {
                const validLang = codeLang && hljs.getLanguage(codeLang) ? codeLang : null;
                const highlighted = validLang
                    ? hljs.highlight(codeText, { language: validLang }).value
                    : hljs.highlightAuto(codeText).value;
                // 用 hljs-pre class 方便 CSS 加背景，code 加 hljs class 触发主题色
                return `<pre class="hljs-pre"><code class="hljs language-${validLang || 'plaintext'}">${highlighted}</code></pre>`;
            }
            const escaped = codeText.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
            return `<pre class="hljs-pre"><code>${escaped}</code></pre>`;
        };

        //表格
        // marked v9+：renderer.table 接收单个 token 对象
        //   token.header = [{text, tokens, ...}, ...]  列头数组
        //   token.rows   = [[{text,...},...], ...]      行数组
        //   token.align  = [null|'left'|'center'|'right', ...]
        // marked v4-v8：renderer.table(header, body) 两个 HTML 字符串
        renderer.table = function (token) {
            if (token && typeof token === 'object' && Array.isArray(token.header)) {
                // marked v9+：手动拼 thead / tbody
                const alignStyle = (i) => token.align && token.align[i] ? ` style="text-align:${token.align[i]}"` : '';

                const theadRow = token.header.map((cell, i) =>
                    `<th${alignStyle(i)}>${cell.text}</th>`
                ).join('');

                const tbodyRows = token.rows.map(row =>
                        '<tr>' + row.map((cell, i) =>
                            `<td${alignStyle(i)}>${cell.text}</td>`
                        ).join('') + '</tr>'
                ).join('');

                return `<div class="table-wrapper"><table class="md-table">` +
                    `<thead><tr>${theadRow}</tr></thead>` +
                    `<tbody>${tbodyRows}</tbody>` +
                    `</table></div>`;
            }

            // marked v4-v8 兼容：两个字符串参数
            const headerStr = typeof token    === 'string' ? token    : '';
            const bodyStr   = typeof arguments[1] === 'string' ? arguments[1] : '';
            return `<div class="table-wrapper"><table class="md-table"><thead>${headerStr}</thead><tbody>${bodyStr}</tbody></table></div>`;
        };

        // ── TodoList ──
        // marked v9+ GFM task list：listitem token = {task: true, checked: bool, text: '<已渲染HTML>', ...}
        // 注意：v9+ 的 token.text 已经是内部渲染后的 HTML，并且 marked 本身会在 text 开头插入
        //       <input disabled="" type="checkbox"> 这个标签，需要去掉再重新输出我们自己的 checkbox。
        renderer.listitem = function (token) {
            if (token && typeof token === 'object') {
                if (token.task === true) {
                    const checked   = token.checked ? 'checked' : '';
                    const cls       = token.checked ? 'todo-item done' : 'todo-item';
                    // 移除 marked 自动插入的 <input ...> 避免出现两个 checkbox
                    const innerHtml = (token.text || '').replace(/^<input\b[^>]*>\s*/i, '');
                    return `<li class="${cls}"><input type="checkbox" ${checked} disabled> ${innerHtml}</li>\n`;
                }
                return `<li>${token.text || ''}</li>\n`;
            }

            // marked v4-v8 兼容：token 是字符串
            const text = String(token);
            if (/^\[x\]\s/i.test(text)) {
                return `<li class="todo-item done"><input type="checkbox" checked disabled> ${text.slice(4)}</li>\n`;
            }
            if (/^\[ \]\s/.test(text)) {
                return `<li class="todo-item"><input type="checkbox" disabled> ${text.slice(4)}</li>\n`;
            }
            return `<li>${text}</li>\n`;
        };

        //  配置 marked
        marked.setOptions({
            renderer,
            gfm: true,          // 开启 GFM：表格、删除线等
            breaks: false,      // 单个换行不变 <br>
            pedantic: false
        });
    }

    function loadArticle(id) {
        state.loadingIndex = layer.load(2, { shade: [0.08, '#000'] });

        if (USE_MOCK) {
            setTimeout(() => {
                const data = getMockArticle(id);
                renderArticle(data);
                closeLoading();
            }, 300);
            return;
        }

        $.ajax({
            url: `${API_BASE}/${id}`,
            method: 'GET',
            dataType: 'json',
            success: function (res) {
                console.log("接口返回：", res); // 调试用

                const data = normalizeResponse(res);
                if (!data) {
                    layer.msg('文章数据为空');
                    renderEmptyPage('文章不存在');
                    closeLoading();
                    return;
                }

                renderArticle(data);
                closeLoading();
            },
            error: function (xhr) {
                closeLoading();
                const msg = getAjaxErrorMessage(xhr) || '文章加载失败';
                layer.msg(msg);
                renderEmptyPage(msg);
            }
        });
    }

    function renderArticle(data) {
        const title = data.title || data.articleTitle || '未命名文章';
        const publishTime = formatTime(data.publishTime);
        const category = data.categoryName
            || data.category_name
            || (data.category && (data.category.categoryName || data.category.name))
            || '未分类';
        const viewCount = data.viewCount ?? 0;

        const tags = data.tags || [];
        const mdContent   = data.contentMd || '';
        const htmlContent = data.content || '';

        $('#title').text(title);
        $('#publishTime').text(publishTime);
        $('#category').text(category);
        $('#viewCount').text(viewCount);

        renderTags(tags);
        renderContent(mdContent,htmlContent);
        renderPrevNext(null, null);
        renderToc();
        bindTocHighlight();

        element.render();
    }

    function renderTags(tags) {
        const $wrap = $('#tags');
        $wrap.empty();

        if (!tags.length) {
            $wrap.hide();
            return;
        }

        tags.forEach(tag => {
            const text = typeof tag === 'string' ? tag : (tag.tagName || tag.name || '');
            if (!text) return;
            const $item = $('<span></span>').addClass('article-tag').text(text);
            $wrap.append($item);
        });

        $wrap.show();
    }

    function renderContent(md, html) {
        const $content = $('#markdown-content');

        if (!md && !html) {
            $content.html('<p class="article-empty" style="text-align:center;color:var(--text-muted);">暂无正文内容</p>');
            return;
        }

        if (md && window.marked) {
            // 前端实时渲染 Markdown（支持表格 / 代码高亮 / TodoList）
            try {
                $content.html(marked.parse(md));
            } catch (e) {
                console.warn('marked 渲染失败，降级为 HTML：', e);
                $content.html(html);
            }
        } else {
            // 使用后端预渲染的 HTML
            $content.html(html);
        }

        enhanceContent();
        renderLatex();     // ← LaTeX 渲染（在 DOM 插入后执行）
    }

    function enhanceContent() {
        const $content = $('#markdown-content');

        // 为超链接统一添加新标签页打开
        $content.find('a').attr('target', '_blank').attr('rel', 'noopener noreferrer');
        $content.find('img').attr('loading', 'lazy');
        // 代码高亮
        if (window.hljs) {
            document.querySelectorAll('#markdown-content pre code:not(.hljs)').forEach(block => {
                try {
                    hljs.highlightElement(block);
                } catch (e) {
                    console.warn("Highlight.js 渲染失败:", e);
                }
            });
        }

        // 图片懒加载
        $content.find('img').attr('loading', 'lazy');
    }
    //latex渲染
    function renderLatex() {
        const contentEl = document.getElementById('markdown-content');
        if (!contentEl) return;

        // 若页面已加载 KaTeX renderMathInElement，直接调用
        if (window.renderMathInElement) {
            renderMathInElement(contentEl, {
                delimiters: [
                    { left: '$$', right: '$$', display: true  },
                    { left: '$',  right: '$',  display: false },
                    { left: '\\(', right: '\\)', display: false },
                    { left: '\\[', right: '\\]', display: true  }
                ],
                throwOnError: false
            });
            return;
        }

        // 若 KaTeX 尚未加载，动态注入
        if (!document.getElementById('katex-css')) {
            const link = document.createElement('link');
            link.id   = 'katex-css';
            link.rel  = 'stylesheet';
            link.href = 'https://cdn.jsdelivr.net/npm/katex/dist/katex.min.css';
            document.head.appendChild(link);
        }

        if (!document.getElementById('katex-js')) {
            const script = document.createElement('script');
            script.id  = 'katex-js';
            script.src = 'https://cdn.jsdelivr.net/npm/katex/dist/katex.min.js';
            script.onload = function () {
                const autoScript = document.createElement('script');
                autoScript.src = 'https://cdn.jsdelivr.net/npm/katex/dist/contrib/auto-render.min.js';
                autoScript.onload = function () {
                    if (window.renderMathInElement) {
                        renderMathInElement(contentEl, {
                            delimiters: [
                                { left: '$$', right: '$$', display: true  },
                                { left: '$',  right: '$',  display: false },
                                { left: '\\(', right: '\\)', display: false },
                                { left: '\\[', right: '\\]', display: true  }
                            ],
                            throwOnError: false
                        });
                    }
                };
                document.head.appendChild(autoScript);
            };
            document.head.appendChild(script);
        }
    }

    function renderToc() {
        const toc = document.getElementById('toc');
        const tocContainer = document.getElementById('toc-container');
        const content = document.getElementById('markdown-content');

        if (!toc || !content || !tocContainer) return;

        toc.innerHTML = '';

        const headings = content.querySelectorAll('h1, h2, h3, h4');
        if (!headings.length) {
            tocContainer.style.display = 'none';
            return;
        }

        tocContainer.style.display = 'block';

        headings.forEach((heading, index) => {
            const id = `heading-${index}`;
            heading.id = id;

            const a = document.createElement('a');
            a.href = `#${id}`;
            a.className = `toc-link`;

            // 简单处理 H3 缩进层级
            if (heading.tagName.toLowerCase() === 'h3') {
                a.classList.add('level-h3');
            }

            a.textContent = heading.textContent;

            a.onclick = function (e) {
                e.preventDefault();
                document.getElementById(id).scrollIntoView({ behavior: 'smooth' });
            };

            toc.appendChild(a);
        });
    }
    //目录滚动高亮
    function bindTocHighlight() {
        const tocLinks = document.querySelectorAll('.toc-link');
        if (!tocLinks.length) return;

        const headingIds = Array.from(tocLinks).map(a => a.getAttribute('href').slice(1));

        window.addEventListener('scroll', NanoBlog.debounce(function () {
            let activeId = headingIds[0];
            for (const id of headingIds) {
                const el = document.getElementById(id);
                if (el && el.getBoundingClientRect().top <= 120) {
                    activeId = id;
                }
            }
            tocLinks.forEach(a => {
                const isActive = a.getAttribute('href') === `#${activeId}`;
                a.classList.toggle('active', isActive);
            });
        }, 50));
    }

    function renderPrevNext(prevArticle, nextArticle) {
        // 暂位逻辑，后续可对接后端数据
        $('#prev-post').addClass('disabled').text('← 没有上一篇');
        $('#next-post').addClass('disabled').text('没有下一篇 →');
    }

    function bindBackToTop() {
        const btn = document.getElementById('back-to-top');
        if (!btn) return;

        window.addEventListener('scroll', () => {
            // 滑动超过 300px 显示按钮
            btn.classList.toggle('show', window.scrollY > 300);
        });

        btn.addEventListener('click', () => {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }

    function bindNavigation() {
        document.addEventListener('click', function (e) {
            const target = e.target.closest('a');
            if (!target) return;
            if (target.classList.contains('disabled')) {
                e.preventDefault();
            }
        });
    }

    function bindMarkdownEnhance() {}

    function renderEmptyPage(message) {
        $('#title').text(message);
        $('#markdown-content').html(`<p style="text-align: center; color: var(--text-muted); padding: 50px 0;">${message}</p>`);
    }

    function closeLoading() {
        if (state.loadingIndex !== null) {
            layer.close(state.loadingIndex);
            state.loadingIndex = null;
        }
    }

    function getQueryParam(name) {
        return new URLSearchParams(window.location.search).get(name);
    }

    // 处理后端不同的 code 规范
    function normalizeResponse(res) {
        if (!res) return null;

        if (typeof res === 'object') {
            if ('code' in res) {
                return (res.code === 0 || res.code === 200) ? res.data : null;
            }
            if ('data' in res) {
                return res.data;
            }
            return res;
        }

        return null;
    }

    function getAjaxErrorMessage(xhr) {
        return xhr?.statusText || '请求失败';
    }

    function formatTime(value) {
        if (!value) return '';
        const date = new Date(value);
        if (isNaN(date)) return value;
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    }
});