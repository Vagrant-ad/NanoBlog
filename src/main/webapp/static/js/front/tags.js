(function (window, document) {
    'use strict';

    /*tags.js:标签页逻辑*/

    /*状态*/
    const state = {
        all:      [],       //原始全量数据(后端已按文章数降序)
        keyword:  '',       //当前生效的搜索关键词
        sortBy:   'count'   //'count'|'name'
    };

    /*DOM引用*/
    const cloud       = document.getElementById('tagsCloud');
    const searchInput = document.getElementById('tagSearchInput');
    const searchBtn   = document.getElementById('tagSearchBtn');
    const clearBtn    = document.getElementById('tagSearchClear');
    const resultInfo  = document.getElementById('tagsResultInfo');
    const sortBtns    = document.querySelectorAll('.sort-tag-btn');

    /*根据文章数占比返回字体大小(4档)*/
    function getFontSize(count, maxCount) {
        if (!maxCount) return '0.88rem';
        const ratio = count / maxCount;
        if (ratio >= 0.8)  return '1.15rem';
        if (ratio >= 0.5)  return '1.0rem';
        if (ratio >= 0.25) return '0.9rem';
        return '0.82rem';
    }

    /*渲染标签云*/
    function render(tags) {
        if (!tags || tags.length === 0) {
            cloud.innerHTML = `
                <div class="tags-empty">
                    <div class="tags-empty-icon"><i class="fas fa-tag"></i></div>
                    <div class="tags-empty-text">
                        ${state.keyword ? '没有找到匹配的标签' : '暂无标签'}
                    </div>
                    ${state.keyword
                ? '<div class="tags-empty-sub">换个关键词试试？</div>'
                : ''}
                </div>`;
            return;
        }

        //热门标签按文章数取前5
        const hotIds = new Set(
            [...state.all]
                .sort((a, b) => (b.articleCount || 0) - (a.articleCount || 0))
                .slice(0, 5)
                .map(t => t.id)
        );
        const maxCount = state.all.length
            ? Math.max(...state.all.map(t => t.articleCount || 0))
            : 0;

        const html = tags.map((tag) => {
            const fontSize = getFontSize(tag.articleCount || 0, maxCount);
            const isHot    = hotIds.has(tag.id);
            const hotIcon  = isHot
                ? '<i class="fas fa-fire" style="color:#f59e0b;font-size:0.72rem;margin-right:-2px;"></i>'
                : '';

            //高亮命中关键词
            let displayName = escHtml(tag.tagName || '');
            if (state.keyword) {
                const re = new RegExp(`(${escRegex(state.keyword)})`, 'gi');
                displayName = displayName.replace(re,
                    '<mark style="background:rgba(163,216,224,0.5);color:var(--brand-blue);'
                    + 'border-radius:2px;padding:0 2px;">$1</mark>'
                );
            }

            return `
                <a class="tag-pill${isHot ? ' is-hot' : ''}"
                   style="--pill-font-size:${fontSize};"
                   href="${NanoBlog.apiBase}/pages/front/index.html?tagId=${tag.id}"
                   title="${escHtml(tag.tagName)} · ${tag.articleCount || 0} 篇文章">
                    ${hotIcon}
                    <span class="tag-name">${displayName}</span>
                    <span class="tag-count-badge">${tag.articleCount || 0}</span>
                </a>`;
        }).join('');

        cloud.innerHTML = html;
    }

    /*过滤+排序+渲染(统一入口)*/
    function applyFilter() {
        const kw = state.keyword.trim().toLowerCase();

        let result = kw
            ? state.all.filter(t => (t.tagName || '').toLowerCase().includes(kw))
            : [...state.all];

        if (state.sortBy === 'name') {
            result.sort((a, b) =>
                (a.tagName || '').localeCompare(b.tagName || '', 'zh-CN'));
        } else {
            result.sort((a, b) => (b.articleCount || 0) - (a.articleCount || 0));
        }

        //更新结果提示
        resultInfo.innerHTML = kw
            ? `搜索 "<span class="highlight">${escHtml(kw)}</span>"，`
            + `找到 <span class="highlight">${result.length}</span> 个标签`
            : `共 <span class="highlight">${result.length}</span> 个标签`;

        render(result);
    }

    /*触发搜索(仅回车或按钮触发)*/
    function doSearch() {
        state.keyword = searchInput.value;
        //清除按钮按输入框内容显隐
        clearBtn.classList.toggle('visible', !!searchInput.value);
        applyFilter();
    }

    /*加载标签数据*/
    function loadTags() {
        fetch(NanoBlog.apiBase + '/tag/list', { credentials: 'same-origin' })
            .then(r => r.json())
            .then(res => {
                if (res.code !== 200 || !res.data) {
                    cloud.innerHTML = `
                        <div class="tags-empty">
                            <div class="tags-empty-icon"><i class="fas fa-exclamation-circle"></i></div>
                            <div class="tags-empty-text">加载失败，请刷新重试</div>
                        </div>`;
                    return;
                }

                state.all = res.data;

                //更新Hero统计
                const tagCount   = res.data.length;
                const articleSum = res.data.reduce((s, t) => s + (t.articleCount || 0), 0);
                const countEl    = document.getElementById('statTagCount');
                const sumEl      = document.getElementById('statArticleSum');
                if (countEl) countEl.textContent = tagCount + ' 个标签';
                if (sumEl)   sumEl.textContent   = articleSum + ' 篇文章';

                applyFilter();
            })
            .catch(() => {
                cloud.innerHTML = `
                    <div class="tags-empty">
                        <div class="tags-empty-icon"><i class="fas fa-wifi"></i></div>
                        <div class="tags-empty-text">网络异常，请稍后重试</div>
                    </div>`;
            });
    }

    /*事件绑定*/

    //搜索按钮点击
    if (searchBtn) {
        searchBtn.addEventListener('click', doSearch);
    }

    //输入框回车
    if (searchInput) {
        searchInput.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                doSearch();
            }
        });

        //仅控制清除按钮显隐,不触发搜索
        searchInput.addEventListener('input', function () {
            clearBtn.classList.toggle('visible', !!this.value);
        });
    }

    //清除按钮
    if (clearBtn) {
        clearBtn.addEventListener('click', function () {
            searchInput.value = '';
            state.keyword = '';
            this.classList.remove('visible');
            searchInput.focus();
            applyFilter();
        });
    }

    //排序切换
    sortBtns.forEach(btn => {
        btn.addEventListener('click', function () {
            sortBtns.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            state.sortBy = this.dataset.sort;
            applyFilter();
        });
    });

    //顶部navbar搜索:跳转首页搜索
    const navSearch    = document.getElementById('searchInput');
    const navSearchBtn = document.getElementById('searchBtn');
    if (navSearch) {
        navSearch.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && this.value.trim()) {
                location.href = NanoBlog.apiBase + '/pages/front/index.html?keyword='
                    + encodeURIComponent(this.value.trim());
            }
        });
    }
    if (navSearchBtn) {
        navSearchBtn.addEventListener('click', function () {
            const val = navSearch ? navSearch.value.trim() : '';
            if (val) {
                location.href = NanoBlog.apiBase + '/pages/front/index.html?keyword='
                    + encodeURIComponent(val);
            }
        });
    }

    /*工具函数*/
    function escHtml(str) {
        return String(str || '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function escRegex(str) {
        return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }

    /*初始化*/
    loadTags();

})(window, document);