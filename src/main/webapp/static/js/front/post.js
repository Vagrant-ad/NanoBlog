layui.use(['layer', 'element'], function () {
    const layer = layui.layer;
    const element = layui.element;
    const $ = layui.$;

    const API_BASE = '/article';
    const CATEGORY_API = '/category/list';

    const state = {
        articleId: getQueryParam('id'),
        loadingIndex: null,
        categoryMap: {} //分类缓存:id->name
    };

    console.log("当前文章ID:", state.articleId);
    init();

    function init() {
        bindBackToTop();
        bindNavigation();
        initMarkdown();

        if (!state.articleId) {
            layer.msg('缺少文章 ID');
            renderEmptyPage('未找到文章');
            return;
        }

        //并行加载分类和文章
        $.when(loadCategoryList(), loadArticleData(state.articleId))
            .done(function (categoryRes, articleRes) {
                //$.when参数[data,status,xhr]
                const categoryData = normalizeResponse(categoryRes[0]);
                const articleData = normalizeResponse(articleRes[0]);

                //构建分类映射
                if (Array.isArray(categoryData)) {
                    categoryData.forEach(cat => {
                        const id = cat.id;
                        const name = cat.categoryName || cat.category_name || cat.name || '';
                        if (id && name) state.categoryMap[id] = name;
                    });
                }

                if (!articleData) {
                    layer.msg('文章数据为空');
                    renderEmptyPage('文章不存在');
                    closeLoading();
                    return;
                }

                renderArticle(articleData);
                closeLoading();
                CommentModule.init(state.articleId);
            })
            .fail(function () {
                closeLoading();
                layer.msg('加载失败，请刷新重试');
                renderEmptyPage('加载失败');
            });
    }

    //请求分类列表,返回Deferred供$.when使用
    function loadCategoryList() {
        return $.ajax({url: CATEGORY_API, method: 'GET', dataType: 'json'});
    }

    //请求文章详情,返回Deferred供$.when使用
    function loadArticleData(id) {
        state.loadingIndex = layer.load(2, {shade: [0.08, '#000']});
        return $.ajax({url: `${API_BASE}/${id}`, method: 'GET', dataType: 'json'});
    }

    //初始化Markdown渲染器(后端返回contentMd时使用)
    function initMarkdown() {
        if (!window.marked) return;

        const renderer = new marked.Renderer();

        renderer.code = function (token) {
            const codeText = (token && typeof token === 'object' && 'text' in token) ? token.text : String(token);
            const codeLang = (token && typeof token === 'object' && 'lang' in token) ? token.lang : arguments[1];
            if (window.hljs) {
                const validLang = codeLang && hljs.getLanguage(codeLang) ? codeLang : null;
                const highlighted = validLang
                    ? hljs.highlight(codeText, {language: validLang}).value
                    : hljs.highlightAuto(codeText).value;
                return `<pre><code class="hljs language-${validLang || 'plaintext'}">${highlighted}</code></pre>`;
            }
            const escaped = codeText.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
            console.log(codeText);
            return `<pre class="hljs-pre"><code>${escaped}</code></pre>`;
        };

        renderer.table = function (token) {
            if (token && typeof token === 'object' && Array.isArray(token.header)) {
                const alignStyle = (i) => token.align && token.align[i] ? ` style="text-align:${token.align[i]}"` : '';
                const theadRow = token.header.map((cell, i) => `<th${alignStyle(i)}>${cell.text}</th>`).join('');
                const tbodyRows = token.rows.map(row =>
                    '<tr>' + row.map((cell, i) => `<td${alignStyle(i)}>${cell.text}</td>`).join('') + '</tr>'
                ).join('');
                return `<div class="table-wrapper"><table class="md-table"><thead><tr>${theadRow}</tr></thead><tbody>${tbodyRows}</tbody></table></div>`;
            }
            const h = typeof token === 'string' ? token : '';
            const b = typeof arguments[1] === 'string' ? arguments[1] : '';
            return `<div class="table-wrapper"><table class="md-table"><thead>${h}</thead><tbody>${b}</tbody></table></div>`;
        };

        renderer.listitem = function (token) {
            if (token && typeof token === 'object') {
                if (token.task === true) {
                    const checked = token.checked ? 'checked' : '';
                    const cls = token.checked ? 'todo-item done' : 'todo-item';
                    const inner = (token.text || '').replace(/^<input\b[^>]*>\s*/i, '');
                    return `<li class="${cls}"><input type="checkbox" ${checked} disabled> ${inner}</li>\n`;
                }
                return `<li>${token.text || ''}</li>\n`;
            }
            const text = String(token);
            if (/^\[x\]\s/i.test(text)) return `<li class="todo-item done"><input type="checkbox" checked disabled> ${text.slice(4)}</li>\n`;
            if (/^\[ \]\s/.test(text)) return `<li class="todo-item"><input type="checkbox" disabled> ${text.slice(4)}</li>\n`;
            return `<li>${text}</li>\n`;
        };
        renderer.link = function (token) {
            const href = token.href || '';
            const text = token.text || href;
            return `<a href="${href}" target="_blank" rel="noopener noreferrer">${text}</a>`;
        };

        renderer.image = function (token) {
            const src = token.href || '';
            const alt = token.text || '';
            return `<img src="${src}" alt="${alt}" loading="lazy" style="max-width:100%;">`;
        };

        renderer.list = function (token) {
            const tag = token.ordered ? 'ol' : 'ul';
            const body = token.items.map(item => renderer.listitem(item)).join('');
            return `<${tag}>${body}</${tag}>`;
        };

        marked.use({renderer, gfm: true, breaks: false, pedantic: false});
    }

    //渲染文章
    function renderArticle(data) {
        const title = data.title || data.articleTitle || data.article_title || '未命名文章';
        const publishTime = formatTime(data.publishTime || data.publish_time);
        const viewCount = data.viewCount ?? data.view_count ?? 0;
        const tags = data.tags || [];

        //优先用categoryId命中本地分类缓存
        const categoryId = data.categoryId || data.category_id;
        const category = (categoryId && state.categoryMap[categoryId])
            || data.categoryName || data.category_name || '未分类';

        const mdContent = data.contentMd || data.content_md || '';
        const htmlContent = data.contentHtml || data.content_html || data.content || '';

        const authorId = data.authorId;
        const authorNickname = data.authorNickname || '匿名作者';
        const authorAvatar = data.authorAvatar || '/static/images/avatar-default.png';

        $('#title').text(title);
        $('#publishTime').text(publishTime);
        $('#category').text(category);
        $('#viewCount').text(viewCount);
        $('#article-author').html(`
        <a href="/pages/front/profile.html" class="article-author-link" title="查看作者主页">
            <img class="article-author-avatar"
                 src="${authorAvatar}"
                 alt="${authorNickname}"
                 onerror="this.src='/static/images/avatar-default.png'">
            <span class="article-author-name">${authorNickname}</span>
        </a>
        `);
        renderTags(tags);
        renderContent(mdContent, htmlContent);
        renderPrevNext();
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
            const text = typeof tag === 'string' ? tag : (tag.tagName || tag.tag_name || tag.name || '');
            if (!text) return;
            $('<span></span>').addClass('article-tag').text(text).appendTo($wrap);
        });
        $wrap.show();
    }

    //渲染正文并做后处理
    function renderContent(md, html) {
        const $content = $('#markdown-content');

        if (!html && !md) {
            $content.html('<p style="text-align:center;color:var(--text-muted);">暂无正文内容</p>');
            return;
        }

        //优先使用后端渲染好的HTML
        $content.html(html || '');

        //后处理:高亮、LaTeX、表格和任务列表修复
        highlightCodeBlocks();
        renderLatex();
        fixInlineMarkdownTables($content);
        fixTodoList($content);
        fixAdmonitions($content);
        enhanceContent($content);
    }

    //修复<p>内的管道表格
    function fixInlineMarkdownTables($content) {
        $content.find('p').each(function () {
            const $p = $(this);
            const raw = $p.html() || '';
            if (!raw.includes('|') || !/\|[\s:]*-+[\s:]*\|/.test(raw)) return;

            const lines = raw.split(/\n/).map(l => l.trim()).filter(l => l.startsWith('|'));
            if (lines.length < 3) return;

            const tableHtml = pipeLinesToTable(lines);
            if (tableHtml) $p.replaceWith(tableHtml);
        });
    }

    function pipeLinesToTable(lines) {
        const parseRow = (line) => line.split('|').slice(1, -1).map(c => c.trim());
        const headers = parseRow(lines[0]);
        const seps = parseRow(lines[1]);

        if (!seps.every(c => /^:?-+:?$/.test(c.trim()))) return null;

        const aligns = seps.map(c => {
            const t = c.trim();
            if (t.startsWith(':') && t.endsWith(':')) return 'center';
            if (t.endsWith(':')) return 'right';
            if (t.startsWith(':')) return 'left';
            return '';
        });

        const alignStyle = (i) => aligns[i] ? ` style="text-align:${aligns[i]}"` : '';

        const thead = '<tr>' + headers.map((cell, i) => `<th${alignStyle(i)}>${cell}</th>`).join('') + '</tr>';
        const tbody = lines.slice(2).map(line => {
            const cells = parseRow(line);
            return '<tr>' + cells.map((cell, i) => `<td${alignStyle(i)}>${cell}</td>`).join('') + '</tr>';
        }).join('');

        return `<div class="table-wrapper"><table class="md-table"><thead>${thead}</thead><tbody>${tbody}</tbody></table></div>`;
    }

    //修复任务列表语法为checkbox
    function fixTodoList($content) {
        $content.find('li').each(function () {
            const $li = $(this);
            const html = $li.html().trim();

            if (/^\[x\]/i.test(html)) {
                $li.addClass('todo-item done')
                    .html('<input type="checkbox" checked disabled> ' + html.slice(3).trim());
            } else if (/^\[ \]/.test(html)) {
                $li.addClass('todo-item')
                    .html('<input type="checkbox" disabled> ' + html.slice(3).trim());
            }
        });
        $content.find('ul:has(li.todo-item)')
            .css({'list-style': 'none', 'padding-left': '4px'});
    }

    //支持GitHub提示块语法
    function fixAdmonitions($content) {
        //支持:> [!TIP] [!NOTE] [!WARNING] [!IMPORTANT] [!CAUTION]
        const typeMap = {
            'TIP': {label: '提示', icon: '💡', color: '#1a7f37', bg: 'rgba(26,127,55,0.08)', border: '#2da44e'},
            'NOTE': {label: '注意', icon: 'ℹ️', color: '#0969da', bg: 'rgba(9,105,218,0.08)', border: '#54aeff'},
            'WARNING': {label: '警告', icon: '⚠️', color: '#9a6700', bg: 'rgba(154,103,0,0.08)', border: '#d4a72c'},
            'IMPORTANT': {label: '重要', icon: '❗', color: '#8250df', bg: 'rgba(130,80,223,0.08)', border: '#a371f7'},
            'CAUTION': {label: '危险', icon: '🔥', color: '#cf222e', bg: 'rgba(207,34,46,0.08)', border: '#ff8182'}
        };

        $content.find('blockquote').each(function () {
            const $bq = $(this);
            const $firstP = $bq.find('p').first();
            if (!$firstP.length) return;

            const firstLine = $firstP.text().trim();
            const match = firstLine.match(/^\[!(TIP|NOTE|WARNING|IMPORTANT|CAUTION)\]/i);
            if (!match) return;

            const type = match[1].toUpperCase();
            const cfg = typeMap[type];
            if (!cfg) return;

            //移除第一行[!TYPE]标记
            const fullHtml = $firstP.html();
            const cleaned = fullHtml.replace(/^\[!(TIP|NOTE|WARNING|IMPORTANT|CAUTION)\]\s*/i, '').trim();
            if (cleaned) {
                $firstP.html(cleaned);
            } else {
                $firstP.remove();
            }

            //转换为admonition样式块
            $bq.addClass('admonition admonition-' + type.toLowerCase());
            $bq.prepend(`
            <div class="admonition-title" style="color:${cfg.color}">
                <span class="admonition-icon">${cfg.icon}</span>
                ${cfg.label}
            </div>
        `);
            $bq.css({
                'background': cfg.bg,
                'border-left-color': cfg.border
            });
        });
    }

    //修复代码块并执行hljs高亮
    function highlightCodeBlocks() {
        if (!window.hljs) return;
        document.querySelectorAll('#markdown-content pre code').forEach(block => {
            if (block.classList.contains('hljs')) return;
            const langClass = Array.from(block.classList).find(c => c.startsWith('language-'));
            let lang = langClass ? langClass.replace('language-', '') : null;
            if (lang && lang.length > 30) lang = null;

            try {
                if (lang && hljs.getLanguage(lang)) {
                    block.innerHTML = hljs.highlight(block.textContent, {language: lang}).value;
                } else {
                    block.innerHTML = hljs.highlightAuto(block.textContent).value;
                }
                block.classList.add('hljs');
            } catch (e) {
                try {
                    block.innerHTML = hljs.highlightAuto(block.textContent).value;
                } catch (e2) {
                    console.warn('hljs 高亮失败：', e2);
                }
            }

            //给pre添加语言标签和复制按钮
            const pre = block.parentElement;
            if (pre?.tagName === 'PRE') {
                pre.classList.add('hljs-pre');
                pre.style.position = 'relative';

                if (lang) {
                    const langLabel = document.createElement('span');
                    langLabel.className = 'code-lang-label';
                    langLabel.textContent = lang;
                    pre.appendChild(langLabel);
                }

                const copyBtn = document.createElement('button');
                copyBtn.className = 'code-copy-btn';
                copyBtn.textContent = '复制';
                copyBtn.addEventListener('click', function () {
                    const text = block.textContent || '';
                    navigator.clipboard.writeText(text).then(() => {
                        copyBtn.textContent = '已复制 ✓';
                        copyBtn.classList.add('copied');
                        setTimeout(() => {
                            copyBtn.textContent = '复制';
                            copyBtn.classList.remove('copied');
                        }, 2000);
                    }).catch(() => {
                        //兼容不支持clipboard API的环境
                        const ta = document.createElement('textarea');
                        ta.value = text;
                        document.body.appendChild(ta);
                        ta.select();
                        document.execCommand('copy');
                        document.body.removeChild(ta);
                        copyBtn.textContent = '已复制 ✓';
                        setTimeout(() => {
                            copyBtn.textContent = '复制';
                        }, 2000);
                    });
                });
                pre.appendChild(copyBtn);
            }
        });
    }

    function enhanceContent($content) {
        $content.find('a').attr('target', '_blank').attr('rel', 'noopener noreferrer');
        $content.find('img').attr('loading', 'lazy');
    }

    function renderLatex() {
        const contentEl = document.getElementById('markdown-content');
        if (!contentEl) return;
        const opts = {
            delimiters: [
                {left: '$$', right: '$$', display: true},
                {left: '$', right: '$', display: false},
                {left: '\\(', right: '\\)', display: false},
                {left: '\\[', right: '\\]', display: true}
            ],
            throwOnError: false
        };
        if (window.renderMathInElement) {
            renderMathInElement(contentEl, opts);
            return;
        }
        if (!document.getElementById('katex-js')) {
            if (!document.getElementById('katex-css')) {
                const link = document.createElement('link');
                link.id = 'katex-css';
                link.rel = 'stylesheet';
                link.href = 'https://cdn.jsdelivr.net/npm/katex/dist/katex.min.css';
                document.head.appendChild(link);
            }
            const script = document.createElement('script');
            script.id = 'katex-js';
            script.src = 'https://cdn.jsdelivr.net/npm/katex/dist/katex.min.js';
            script.onload = function () {
                const auto = document.createElement('script');
                auto.src = 'https://cdn.jsdelivr.net/npm/katex/dist/contrib/auto-render.min.js';
                auto.onload = () => window.renderMathInElement && renderMathInElement(contentEl, opts);
                document.head.appendChild(auto);
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
            a.className = 'toc-link';
            if (['h3', 'h4'].includes(heading.tagName.toLowerCase())) a.classList.add('level-h3');
            a.textContent = heading.textContent;
            a.onclick = e => {
                e.preventDefault();
                document.getElementById(id).scrollIntoView({behavior: 'smooth'});
            };
            toc.appendChild(a);
        });
    }

    function bindTocHighlight() {
        const tocLinks = document.querySelectorAll('.toc-link');
        if (!tocLinks.length) return;
        const headingIds = Array.from(tocLinks).map(a => a.getAttribute('href').slice(1));
        window.addEventListener('scroll', NanoBlog.debounce(function () {
            let activeId = headingIds[0];
            for (const hid of headingIds) {
                const el = document.getElementById(hid);
                if (el && el.getBoundingClientRect().top <= 120) activeId = hid;
            }
            tocLinks.forEach(a => a.classList.toggle('active', a.getAttribute('href') === `#${activeId}`));
        }, 50));
    }

    function renderPrevNext() {
        $('#prev-post').addClass('disabled').text('← 没有上一篇');
        $('#next-post').addClass('disabled').text('没有下一篇 →');
    }

    function bindBackToTop() {
        const btn = document.getElementById('back-to-top');
        if (!btn) return;
        window.addEventListener('scroll', () => btn.classList.toggle('show', window.scrollY > 300));
        btn.addEventListener('click', () => window.scrollTo({top: 0, behavior: 'smooth'}));
    }

    function bindNavigation() {
        document.addEventListener('click', function (e) {
            const target = e.target.closest('a');
            if (target && target.classList.contains('disabled')) e.preventDefault();
        });
    }

    function renderEmptyPage(message) {
        $('#title').text(message);
        $('#markdown-content').html(`<p style="text-align:center;color:var(--text-muted);padding:50px 0;">${message}</p>`);
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

    function normalizeResponse(res) {
        if (!res) return null;
        if (typeof res === 'object') {
            if ('code' in res) return (res.code === 0 || res.code === 200) ? res.data : null;
            if ('data' in res) return res.data;
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
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ` +
            `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    }
});