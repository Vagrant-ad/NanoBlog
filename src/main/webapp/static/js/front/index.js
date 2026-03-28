(function (window, document) {
    'use strict';

    const PAGE_SIZE = 8;
    const DEFAULT_COVER = '/static/images/demo-cover.jpg';
    const DEFAULT_AVATAR = '/static/images/avatar-default.png';

    let currentPage = 1;
    let currentKeyword = '';

    const articleGrid = document.querySelector('.article-grid');
    const searchInput = document.querySelector('.search-box input');
    const paginationEl = document.getElementById('pagination');

    let laypageInstance = null;

    function getApiUrl(page, size, keyword) {
        const params = new URLSearchParams();
        params.set('page', String(page));
        params.set('size', String(size));

        if (keyword && keyword.trim()) {
            params.set('keyword', keyword.trim());
        }

        return `/article/home?${params.toString()}`;
    }

    function renderEmptyState(message = '暂无文章') {
        if (!articleGrid) return;

        articleGrid.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-title">${message}</div>
                <div class="empty-state-desc">去后台发布第一篇文章吧</div>
            </div>
        `;
    }

    function renderArticles(records) {
        if (!articleGrid) return;

        if (!records || records.length === 0) {
            renderEmptyState('暂无文章');
            return;
        }

        const html = records.map(item => {
            const id = item.id ?? '';
            const title = NanoBlog.escapeHtml(item.title || '未命名文章');
            const summary = NanoBlog.escapeHtml(item.summary || '暂无摘要');
            const coverUrl = item.coverUrl || DEFAULT_COVER;
            const authorName = NanoBlog.escapeHtml(item.authorName || '匿名');
            const authorAvatar = item.authorAvatar || DEFAULT_AVATAR;
            const categoryName = NanoBlog.escapeHtml(item.categoryName || '');
            const publishTime = NanoBlog.formatDate(item.publishTime, true);
            const viewCount = item.viewCount ?? 0;
            const likeCount = item.likeCount ?? 0;
            const commentCount = item.commentCount ?? 0;
            const tags = NanoBlog.normalizeTags(item.tags);

            const badgeHtml = `
                ${(item.isTop ? '<span class="card-badge card-badge-top">置顶</span>' : '')}
                ${(item.isFeatured ? '<span class="card-badge card-badge-featured">推荐</span>' : '')}
            `;

            const tagHtml = tags.length > 0
                ? tags.map(tag => `<span class="tag">${NanoBlog.escapeHtml(tag)}</span>`).join('')
                : '<span class="tag tag-empty">暂无标签</span>';

            return `
                <article class="article-card">
                    <a class="card-link" href="/pages/front/post.html?id=${id}">
                        <div class="card-cover">
                            <img src="${coverUrl}" alt="${title} 封面" loading="lazy" onerror="this.src='${DEFAULT_COVER}'">
                            <div class="card-badges">
                                ${badgeHtml}
                            </div>
                        </div>

                        <div class="card-body">
                            <div class="card-header-line">
                                <span class="card-category">${categoryName}</span>
                            </div>

                            <h2 class="card-title">${title}</h2>

                            <p class="card-summary">${summary}</p>

                            <div class="card-tags">
                                ${tagHtml}
                            </div>

                            <div class="card-meta">
                                <div class="card-author">
                                    <img class="author-avatar" src="${authorAvatar}" alt="${authorName}" onerror="this.src='${DEFAULT_AVATAR}'">
                                    <span class="author-name">${authorName}</span>
                                </div>

                                <div class="card-stats">
                                    <span>${publishTime}</span>
                                    <span>·</span>
                                    <span>${viewCount} 浏览</span>
                                    <span>·</span>
                                    <span>${likeCount} 赞</span>
                                    <span>·</span>
                                    <span>${commentCount} 评论</span>
                                </div>
                            </div>
                        </div>
                    </a>
                </article>
            `;
        }).join('');

        articleGrid.innerHTML = html;
    }

    function renderPagination(total) {
        if (!paginationEl || !window.layui) return;

        layui.use(['laypage'], function () {
            const laypage = layui.laypage;

            laypage.render({
                elem: 'pagination',
                count: total || 0,
                limit: PAGE_SIZE,
                curr: currentPage,
                layout: ['prev', 'page', 'next', 'count'],
                jump: function (obj, first) {
                    if (!first && obj.curr !== currentPage) {
                        loadArticles(obj.curr, currentKeyword);
                    }
                }
            });

            laypageInstance = laypage;
        });
    }

    async function loadArticles(page = 1, keyword = '') {
        currentPage = page;
        currentKeyword = keyword;

        if (articleGrid) {
            articleGrid.classList.add('is-loading');
        }

        try {
            const result = await NanoBlog.request(getApiUrl(page, PAGE_SIZE, keyword));
            const pageData = result?.data || result || {};
            const records = pageData.records || [];
            const total = pageData.total || 0;

            renderArticles(records);
            renderPagination(total);

        } catch (error) {
            console.error(error);
            NanoBlog.toast(error.message || '文章加载失败');
            renderEmptyState('加载失败');
        } finally {
            if (articleGrid) {
                articleGrid.classList.remove('is-loading');
            }
        }
    }

    function bindSearch() {
        if (!searchInput) return;

        const triggerSearch = NanoBlog.debounce(() => {
            const keyword = searchInput.value.trim();
            loadArticles(1, keyword);
        }, 300);

        searchInput.addEventListener('input', triggerSearch);

        searchInput.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                loadArticles(1, searchInput.value.trim());
            }
        });
    }

    function bindSearchBoxFocus() {
        const searchBox = document.querySelector('.search-box');
        if (!searchBox || !searchInput) return;

        searchBox.addEventListener('click', function () {
            searchInput.focus();
        });
    }

    function init() {
        bindSearch();
        bindSearchBoxFocus();
        loadArticles(1, '');
    }

    document.addEventListener('DOMContentLoaded', function () {
        if (!window.layui) {
            console.error('layui 未加载');
            return;
        }

        layui.use(['layer', 'laypage'], function () {
            init();
        });
    });

})(window, document);