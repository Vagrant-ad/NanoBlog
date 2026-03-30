layui.use(['layer', 'element'], function () {
    const layer = layui.layer;
    const element = layui.element;
    const $ = layui.$;

    const API_BASE      = '/article';
    const CATEGORY_API  = '/category/list';

    const state = {
        articleId: getQueryParam('id'),
        loadingIndex: null,
        categoryMap: {}   // { id -> categoryName } 缓存
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

        // 并行请求：分类列表 + 文章详情，两者都完成后再渲染
        $.when(loadCategoryList(), loadArticleData(state.articleId))
            .done(function (categoryRes, articleRes) {
                // $.when 的参数是每个 ajax 的 [data, status, xhr] 数组
                const categoryData = normalizeResponse(categoryRes[0]);
                const articleData  = normalizeResponse(articleRes[0]);

                // 构建 id -> categoryName 映射
                if (Array.isArray(categoryData)) {
                    categoryData.forEach(cat => {
                        const id   = cat.id;
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
            })
            .fail(function () {
                closeLoading();
                layer.msg('加载失败，请刷新重试');
                renderEmptyPage('加载失败');
            });
    }

    // ─────────────────────────────────────────────
    // 请求分类列表（返回 Deferred，供 $.when 使用）
    // ─────────────────────────────────────────────
    function loadCategoryList() {
        return $.ajax({ url: CATEGORY_API, method: 'GET', dataType: 'json' });
    }

    // ─────────────────────────────────────────────
    // 请求文章详情（返回 Deferred，供 $.when 使用）
    // ─────────────────────────────────────────────
    function loadArticleData(id) {
        state.loadingIndex = layer.load(2, { shade: [0.08, '#000'] });
        return $.ajax({ url: `${API_BASE}/${id}`, method: 'GET', dataType: 'json' });
    }

    // ─────────────────────────────────────────────
    // Markdown 初始化（备用，后端若返回 contentMd 时生效）
    // ─────────────────────────────────────────────
    function initMarkdown() {
        if (!window.marked) return;

        const renderer = new marked.Renderer();

        renderer.code = function (token) {
            const codeText = (token && typeof token === 'object' && 'text' in token) ? token.text : String(token);
            const codeLang = (token && typeof token === 'object' && 'lang' in token) ? token.lang : arguments[1];
            if (window.hljs) {
                const validLang = codeLang && hljs.getLanguage(codeLang) ? codeLang : null;
                const highlighted = validLang
                    ? hljs.highlight(codeText, { language: validLang }).value
                    : hljs.highlightAuto(codeText).value;
                return `<pre><code class="hljs language-${validLang || 'plaintext'}">${highlighted}</code></pre>`;            }
            const escaped = codeText.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
            console.log(codeText);
            return `<pre class="hljs-pre"><code>${escaped}</code></pre>`;
        };

        renderer.table = function (token) {
            if (token && typeof token === 'object' && Array.isArray(token.header)) {
                const alignStyle = (i) => token.align && token.align[i] ? ` style="text-align:${token.align[i]}"` : '';
                const theadRow  = token.header.map((cell, i) => `<th${alignStyle(i)}>${cell.text}</th>`).join('');
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
                    const cls     = token.checked ? 'todo-item done' : 'todo-item';
                    const inner   = (token.text || '').replace(/^<input\b[^>]*>\s*/i, '');
                    return `<li class="${cls}"><input type="checkbox" ${checked} disabled> ${inner}</li>\n`;
                }
                return `<li>${token.text || ''}</li>\n`;
            }
            const text = String(token);
            if (/^\[x\]\s/i.test(text)) return `<li class="todo-item done"><input type="checkbox" checked disabled> ${text.slice(4)}</li>\n`;
            if (/^\[ \]\s/.test(text))  return `<li class="todo-item"><input type="checkbox" disabled> ${text.slice(4)}</li>\n`;
            return `<li>${text}</li>\n`;
        };
        // 在 initMarkdown 里，marked.use 之前加上：

        renderer.link = function(token) {
            const href = token.href || '';
            const text = token.text || href;
            return `<a href="${href}" target="_blank" rel="noopener noreferrer">${text}</a>`;
        };

        renderer.image = function(token) {
            const src = token.href || '';
            const alt = token.text || '';
            return `<img src="${src}" alt="${alt}" loading="lazy" style="max-width:100%;">`;
        };

        renderer.list = function(token) {
            const tag = token.ordered ? 'ol' : 'ul';
            const body = token.items.map(item => renderer.listitem(item)).join('');
            return `<${tag}>${body}</${tag}>`;
        };

        marked.use({ renderer, gfm: true, breaks: false, pedantic: false });
    }

    // ─────────────────────────────────────────────
    // 渲染文章
    // ─────────────────────────────────────────────
    function renderArticle(data) {
        const title       = data.title || data.articleTitle || data.article_title || '未命名文章';
        const publishTime = formatTime(data.publishTime || data.publish_time);
        const viewCount   = data.viewCount ?? data.view_count ?? 0;
        const tags        = data.tags || [];

        // ★ 用 categoryId 在本地缓存里查分类名
        const categoryId  = data.categoryId || data.category_id;
        const category    = (categoryId && state.categoryMap[categoryId])
            || data.categoryName || data.category_name || '未分类';

        const mdContent   = data.contentMd || data.content_md || '';
        const htmlContent = data.contentHtml || data.content_html || data.content || '';

        $('#title').text(title);
        $('#publishTime').text(publishTime);
        $('#category').text(category);
        $('#viewCount').text(viewCount);

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
        if (!tags.length) { $wrap.hide(); return; }
        tags.forEach(tag => {
            const text = typeof tag === 'string' ? tag : (tag.tagName || tag.tag_name || tag.name || '');
            if (!text) return;
            $('<span></span>').addClass('article-tag').text(text).appendTo($wrap);
        });
        $wrap.show();
    }


    // 渲染内容 + 后处理（修复后端 HTML 里未渲染的 Markdown 元素）

    /*function renderContent(md, html) {
        const $content = $('#markdown-content');

        if (!md && !html) {
            $content.html('<p class="article-empty" style="text-align:center;color:var(--text-muted);">暂无正文内容</p>');
            return;
        }

        if (md && window.marked) {
            try { $content.html(marked.parse(md)); }
            catch (e) { console.warn('marked 渲染失败，降级为 HTML：', e); $content.html(html); }
        } else {
            $content.html(html);
        }

        // 后处理：修复后端 HTML 里遗漏的 Markdown 元素
        fixInlineMarkdownTables($content);
        fixTodoList($content);
        enhanceContent($content);
        highlightCodeBlocks();
        renderLatex();
    }*/
    function renderContent(md, html) {
        const $content = $('#markdown-content');

        if (!html && !md) {
            $content.html('<p style="text-align:center;color:var(--text-muted);">暂无正文内容</p>');
            return;
        }

        // 优先用后端渲染好的 HTML，不在前端重复解析 md
        // 后端 commonmark 已经处理好列表、链接、图片等所有标准元素
        $content.html(html || '');

        // 后处理：代码高亮、LaTeX、链接增强
        highlightCodeBlocks();
        renderLatex();
        fixInlineMarkdownTables($content);
        fixTodoList($content);
        enhanceContent($content);
        // 保留fixInlineMarkdownTables 和 fixTodoList，覆盖commonmark的表格和todolist渲染错误
    }
    // ── 修复一：<p> 里的 Markdown 管道表格 → <table> ──
    function fixInlineMarkdownTables($content) {
        $content.find('p').each(function () {
            const $p    = $(this);
            const raw   = $p.html() || '';
            if (!raw.includes('|') || !/\|[\s:]*-+[\s:]*\|/.test(raw)) return;

            const lines = raw.split(/\n/).map(l => l.trim()).filter(l => l.startsWith('|'));
            if (lines.length < 3) return;

            const tableHtml = pipeLinesToTable(lines);
            if (tableHtml) $p.replaceWith(tableHtml);
        });
    }

    function pipeLinesToTable(lines) {
        const parseRow = (line) => line.split('|').slice(1, -1).map(c => c.trim());
        const headers  = parseRow(lines[0]);
        const seps     = parseRow(lines[1]);

        if (!seps.every(c => /^:?-+:?$/.test(c.trim()))) return null;

        const aligns = seps.map(c => {
            const t = c.trim();
            if (t.startsWith(':') && t.endsWith(':')) return 'center';
            if (t.endsWith(':'))   return 'right';
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

    // ── 修复二：<li> 里的 [x]/[ ] 文本 → checkbox ──
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
            .css({ 'list-style': 'none', 'padding-left': '4px' });
    }

    // ── 修复三：<pre><code class="language-*"> → hljs 高亮 ──
    function highlightCodeBlocks() {
        if (!window.hljs) return;
        document.querySelectorAll('#markdown-content pre code').forEach(block => {
            if (block.classList.contains('hljs')) return;
            const langClass = Array.from(block.classList).find(c => c.startsWith('language-'));
            let lang = langClass ? langClass.replace('language-', '') : null;
            //防止整段代码被当成语言名
            if (lang && lang.length > 30) {
                lang = null;
            }
            try {
                try {
                    if (lang && hljs.getLanguage(lang)) {
                        block.innerHTML = hljs.highlight(block.textContent, { language: lang }).value;
                    } else {
                        block.innerHTML = hljs.highlightAuto(block.textContent).value;
                    }
                } catch (e) {
                    console.warn('hljs fallback:', e);
                    block.innerHTML = hljs.highlightAuto(block.textContent).value;
                }
                block.classList.add('hljs');
                if (block.parentElement?.tagName === 'PRE') {
                    block.parentElement.classList.add('hljs-pre');
                }
            } catch (e) { console.warn('hljs 高亮失败：', e); }
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
                { left: '$$',  right: '$$',  display: true  },
                { left: '$',   right: '$',   display: false },
                { left: '\\(', right: '\\)', display: false },
                { left: '\\[', right: '\\]', display: true  }
            ],
            throwOnError: false
        };
        if (window.renderMathInElement) { renderMathInElement(contentEl, opts); return; }
        if (!document.getElementById('katex-js')) {
            if (!document.getElementById('katex-css')) {
                const link = document.createElement('link');
                link.id = 'katex-css'; link.rel = 'stylesheet';
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
        if (!headings.length) { tocContainer.style.display = 'none'; return; }
        tocContainer.style.display = 'block';
        headings.forEach((heading, index) => {
            const id = `heading-${index}`;
            heading.id = id;
            const a = document.createElement('a');
            a.href = `#${id}`; a.className = 'toc-link';
            if (['h3','h4'].includes(heading.tagName.toLowerCase())) a.classList.add('level-h3');
            a.textContent = heading.textContent;
            a.onclick = e => { e.preventDefault(); document.getElementById(id).scrollIntoView({ behavior: 'smooth' }); };
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
        btn.addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));
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
        if (state.loadingIndex !== null) { layer.close(state.loadingIndex); state.loadingIndex = null; }
    }

    function getQueryParam(name) { return new URLSearchParams(window.location.search).get(name); }

    function normalizeResponse(res) {
        if (!res) return null;
        if (typeof res === 'object') {
            if ('code' in res) return (res.code === 0 || res.code === 200) ? res.data : null;
            if ('data' in res) return res.data;
            return res;
        }
        return null;
    }

    function getAjaxErrorMessage(xhr) { return xhr?.statusText || '请求失败'; }

    function formatTime(value) {
        if (!value) return '';
        const date = new Date(value);
        if (isNaN(date)) return value;
        return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')} ` +
            `${String(date.getHours()).padStart(2,'0')}:${String(date.getMinutes()).padStart(2,'0')}`;
    }
});