(function (window, document) {
    'use strict';

    const PAGE_SIZE = 8;
    const DEFAULT_COVER = NanoBlog.apiBase + '/static/images/demo-cover.jpg';
    const DEFAULT_AVATAR = NanoBlog.apiBase + '/static/images/avatar-default.png';

    let currentPage = 1;
    //排序关键字
    let currentKeyword = '';
    //排序模式
    let currentSort = 'time';
    //分类&标签id
    let currentCategoryId = NanoBlog.getQueryParam('categoryId') || '';
    let currentTagId = NanoBlog.getQueryParam('tagId') || '';

    const articleGrid = document.querySelector('.article-grid');
    const searchInput = document.getElementById('searchInput');
    const paginationEl = document.getElementById('pagination');

    let laypageInstance = null;

    //获取url
    function getApiUrl(page, size, keyword,sort) {
        const params = new URLSearchParams();
        params.set('page', String(page));
        params.set('size', String(size));
        if (keyword && keyword.trim()) {
            params.set('keyword', keyword.trim());
        }
        if (sort) {
            params.set('sortBy', sort);
        }
        if (currentCategoryId)
            params.set('categoryId', currentCategoryId);
        if (currentTagId)
            params.set('tagId', currentTagId);
        return `/article/home?${params.toString()}`;
    }
    //渲染空状态
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
                    <a class="card-link" href="${NanoBlog.apiBase}/pages/front/post.html?id=${id}">
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
                                    <span><i class="fas fa-clock"></i> ${publishTime}</span>
                                    <span>·</span>
                                    <span><i class="fas fa-eye"></i> ${viewCount}</span>
                                    <span>·</span>
                                    <span><i class="fas fa-thumbs-up"></i> ${likeCount}</span>
                                    <span>·</span>
                                    <span><i class="fas fa-comment"></i> ${commentCount}</span>
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

    async function loadArticles(page = 1, keyword = '',sort='time') {
        currentPage = page;
        currentKeyword = keyword;
        currentSort = sort;
        if (articleGrid) {
            articleGrid.classList.add('is-loading');
        }

        try {
            const result = await NanoBlog.request(getApiUrl(page, PAGE_SIZE, keyword, sort));
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
        //回车触发
        searchInput.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                loadArticles(1, searchInput.value.trim());
            }
        });
        //按钮触发
        const searchBtn = document.getElementById('searchBtn');
        if (searchBtn) {
            searchBtn.addEventListener('click', function() {
                currentKeyword = searchInput.value.trim();
                loadArticles(1, currentKeyword, currentSort);
            });
        }
    }
    //绑定排序按钮
    function bindSortTabs() {
        const sortBtns = document.querySelectorAll('.sort-btn');
        if (!sortBtns.length) return;

        sortBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                //切换active样式
                sortBtns.forEach(b => b.classList.remove('active'));
                this.classList.add('active');
                //更新排序并从第一页加载
                currentSort = this.dataset.sort;
                loadArticles(1, currentKeyword, currentSort);
            });
        });
    }

    function bindSearchBoxFocus() {
        const searchBox = document.querySelector('.search-box');
        if (!searchBox || !searchInput) return;

        searchBox.addEventListener('click', function () {
            searchInput.focus();
        });
    }
    //筛选条件
    function renderFilterBadge() {
        const header = document.querySelector('.section-header');
        if (!header) return;

        if (currentCategoryId) {
            // 查分类名称
            fetch(NanoBlog.apiBase + '/category/list')
                .then(res => res.json())
                .then(result => {
                    if (result.code !== 200) return;
                    const cat = result.data.find(c => String(c.id) === currentCategoryId);
                    if (cat) showFilterBadge('分类', cat.categoryName, header);
                });
        }
        if (currentTagId) {
            fetch(NanoBlog.apiBase + '/tag/list')
                .then(res => res.json())
                .then(result => {
                    if (result.code !== 200) return;
                    const tag = result.data.find(t => String(t.id) === currentTagId);
                    if (tag) showFilterBadge('标签', tag.tagName, header);
                });
        }
    }
    //显示筛选条件
    function showFilterBadge(type, name, header) {
        //构建清除当前筛选条件后的URL
        const params = new URLSearchParams(window.location.search);
        const paramKey = type === '分类' ? 'categoryId' : 'tagId';
        params.delete(paramKey);
        const clearUrl = NanoBlog.apiBase + '/pages/front/index.html' + (params.toString() ? '?' + params.toString() : '');
        //渲染
        const badge = document.createElement('div');
        badge.className = 'filter-badge';
        badge.innerHTML = `
        <span class="filter-badge-type">${type}</span>
        <span class="filter-badge-name">${name}</span>
        <a href="${clearUrl}" class="filter-badge-clear" title="清除筛选">×</a>
    `;
        header.appendChild(badge);
    }

    //hero section统计数据
    function setStatNum(id, val) {
        var el = document.getElementById(id);
        if (!el) return;
        var n = parseInt(val, 10) || 0;
        el.textContent = n >= 1000 ? (n / 1000).toFixed(1) + 'k' : n;
    }

    function loadHeroStats() {
        // heroArticleCount / heroUserCount / heroCommentCount 由 /article/stats 提供
        // heroTagCount 由 /tag/list 提供
        // 两个请求并行，各自静默失败不影响页面其余功能
        fetch(NanoBlog.apiBase + '/article/stats', { credentials: 'same-origin' })
            .then(function (r) { return r.json(); })
            .then(function (res) {
                if (res.code === 200 && res.data) {
                    setStatNum('heroArticleCount', res.data.articleCount);
                    setStatNum('heroUserCount',    res.data.userCount);
                    setStatNum('heroCommentCount', res.data.commentCount);
                }
            })
            .catch(function () {});

        fetch(NanoBlog.apiBase + '/tag/list', { credentials: 'same-origin' })
            .then(function (r) { return r.json(); })
            .then(function (res) {
                if (res.code === 200 && res.data) {
                    setStatNum('heroTagCount', res.data.length);
                }
            })
            .catch(function () {});
    }

    //初始化
    function init() {
        bindSearch();
        bindSortTabs();
        bindSearchBoxFocus();
        renderFilterBadge();
        loadArticles(1, '');
        loadHeroStats();
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