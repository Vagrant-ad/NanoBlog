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
        bindMarkdownEnhance();

        if (!state.articleId) {
            layer.msg('缺少文章 ID');
            renderEmptyPage('未找到文章');
            return;
        }

        loadArticle(state.articleId);
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
        const category = data.categoryName || '未分类';
        const viewCount = data.viewCount ?? 0;

        const tags = data.tags || [];
        const html = data.contentHtml || data.content || data.articleContent || '';

        $('#title').text(title);
        $('#publishTime').text(publishTime);
        $('#category').text(category);
        $('#viewCount').text(viewCount);

        renderTags(tags);
        renderContent(html);

        renderPrevNext(null, null);
        renderToc();

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

    function renderContent(html) {
        const $content = $('#markdown-content');

        if (!html) {
            $content.html('<p class="article-empty" style="text-align: center; color: var(--text-muted);">暂无正文内容</p>');
            return;
        }

        $content.html(html);
        enhanceContent();
    }

    function enhanceContent() {
        const $content = $('#markdown-content');

        // 为超链接统一添加新标签页打开
        $content.find('a').attr('target', '_blank').attr('rel', 'noopener noreferrer');

        // 代码高亮
        if (window.hljs) {
            document.querySelectorAll('#markdown-content pre code').forEach(block => {
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