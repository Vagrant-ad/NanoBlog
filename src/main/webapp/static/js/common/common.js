(function (window, document) {
    'use strict';

    // 前端通用工具
    const NanoBlog = {
        apiBase: ''
    };

    // 统一请求封装
    // 处理JSON序列化、HTTP 状态和业务code
    NanoBlog.request = async function (url, options = {}) {
        const config = {
            method: 'GET',
            credentials: 'same-origin',
            headers: {},
            ...options
        };

        if (config.body && typeof config.body === 'object' && !(config.body instanceof FormData)) {
            config.headers['Content-Type'] = 'application/json;charset=UTF-8';
            config.body = JSON.stringify(config.body);
        }

        const response = await fetch(NanoBlog.apiBase + url, config);
        const contentType = response.headers.get('content-type') || '';
        let data;

        if (contentType.includes('application/json')) {
            data = await response.json();
        } else {
            data = await response.text();
        }

        if (!response.ok) {
            const msg = typeof data === 'string' ? data : (data.msg || '请求失败');
            throw new Error(msg);
        }

        if (data && typeof data === 'object' && 'code' in data && data.code !== 200) {
            throw new Error(data.msg || '请求失败');
        }

        return data;
    };

    // 延迟执行高频触发函数
    NanoBlog.debounce = function (fn, delay = 300) {
        let timer = null;
        return function (...args) {
            clearTimeout(timer);
            timer = setTimeout(() => fn.apply(this, args), delay);
        };
    };

    // 读取URL查询参数
    NanoBlog.getQueryParam = function (name) {
        const params = new URLSearchParams(window.location.search);
        return params.get(name);
    };

    // 转义HTML，避免XSS
    NanoBlog.escapeHtml = function (str) {
        if (str === null || str === undefined) return '';
        return String(str)
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
    };

    // 格式化日期字符串
    NanoBlog.formatDate = function (value, withTime = true) {
        if (!value) return '';

        const date = value instanceof Date ? value : new Date(value);
        if (isNaN(date.getTime())) return '';

        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');

        if (!withTime) {
            return `${y}-${m}-${d}`;
        }

        const hh = String(date.getHours()).padStart(2, '0');
        const mm = String(date.getMinutes()).padStart(2, '0');
        const ss = String(date.getSeconds()).padStart(2, '0');

        return `${y}-${m}-${d} ${hh}:${mm}:${ss}`;
    };

    // 按长度截断文本
    NanoBlog.truncateText = function (text, maxLen = 120) {
        if (!text) return '';
        const str = String(text).trim();
        if (str.length <= maxLen) return str;
        return str.slice(0, maxLen).trimEnd() + '...';
    };

    // 统一标签格式：数组或逗号分隔字符串
    NanoBlog.normalizeTags = function (tags) {
        if (!tags) return [];

        if (Array.isArray(tags)) return tags.filter(Boolean);

        if (typeof tags === 'string') {
            return tags
                .split(',')
                .map(t => t.trim())
                .filter(Boolean);
        }

        return [];
    };

    // 统一提示：优先 layer，兜底 alert
    NanoBlog.toast = function (msg, icon = 2) {
        if (window.layui && layui.layer) {
            layui.layer.msg(msg, { icon, time: 1800 });
        } else {
            alert(msg);
        }
    };

    // 根据当前路径高亮导航
    NanoBlog.setActiveNav = function () {
        const currentPath = window.location.pathname;
        const navLinks = document.querySelectorAll('.navbar-menu a');

        navLinks.forEach(link => {
            const href = link.getAttribute('href') || '';
            const fileName = href.split('/').pop();

            if (fileName && currentPath.endsWith(fileName)) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });
    };

    // 页面滚动时切换导航栏样式
    NanoBlog.bindNavbarScrollEffect = function () {
        const navbar = document.querySelector('.navbar');
        if (!navbar) return;

        const update = () => {
            if (window.scrollY > 0) {
                navbar.classList.add('is-scrolled');
            } else {
                navbar.classList.remove('is-scrolled');
            }
        };

        update();
        window.addEventListener('scroll', update, { passive: true });
    };

    // 初始化导航用户区域（登录态/未登录态）
    NanoBlog.initNavUser = function () {
        const container = document.getElementById('navUserArea');
        if (!container) return;

        fetch('/user/getProfile', { credentials: 'same-origin' })
            .then(res => res.json())
            .then(result => {
                if (result.code === 200 && result.data) {
                    const user = result.data.user;
                    const avatar = user.avatarUrl || '/static/images/avatar-default.png';
                    const nickname = user.nickname || user.username || '用户';

                    container.innerHTML = `
                    <div class="nav-user-dropdown">
                        <img class="nav-avatar" src="${avatar}" alt="${nickname}"
                             onerror="this.src='/static/images/avatar-default.png'">
                        <div class="nav-user-menu">
                            <div class="nav-user-info">
                                <img src="${avatar}" alt="${nickname}"
                                     onerror="this.src='/static/images/avatar-default.png'">
                                <div>
                                    <div class="nav-user-name">${nickname}</div>
                                    <div class="nav-user-username">@${user.username}</div>
                                </div>
                            </div>
                            <div class="nav-menu-divider"></div>
                            <a class="nav-menu-item" href="/pages/front/profile.html">
                                <i class="layui-icon layui-icon-username"></i> 个人资料
                            </a>
                            <a class="nav-menu-item" href="/pages/front/editor.html">
                                <i class="layui-icon layui-icon-edit"></i> 写文章
                            </a>
                            <div class="nav-menu-divider"></div>
                            <button class="nav-menu-item nav-menu-logout" id="navLogoutBtn">
                                <i class="layui-icon layui-icon-logout"></i>  退出登录
                            </button>
                        </div>
                    </div>
                `;

                    // 退出登录
                    document.getElementById('navLogoutBtn').addEventListener('click', function () {
                        fetch('/user/logout', { method: 'POST', credentials: 'same-origin' })
                            .then(() => {
                                window.location.href = '/pages/front/index.html';
                            });
                    });

                } else {
                    // 未登录时显示登录/注册入口
                    container.innerHTML = `
                    <a href="/pages/front/login.html" class="nav-link">登录</a>
                    <a href="/pages/front/register.html" class="nav-button">注册</a>
                `;
                }
            })
            .catch(() => {
                // 获取用户信息失败时按未登录处理
                container.innerHTML = `
                <a href="/pages/front/login.html" class="nav-link">登录</a>
                <a href="/pages/front/register.html" class="nav-button">注册</a>
            `;
            });
    };
    //初始化分类多级菜单
    NanoBlog.initCategoryMenu = function () {
        const dropdown = document.getElementById('categoryDropdown');
        if (!dropdown) return;

        fetch('/category/tree')
            .then(res => res.json())
            .then(result => {
                if (result.code !== 200 || !result.data) return;
                const categories = result.data;

                dropdown.innerHTML = categories.map(parent => {
                    const hasChildren = parent.children && parent.children.length > 0;

                    const subItems = hasChildren
                        ? parent.children.map(child => `
                        <li class="dropdown-subitem"
                            data-category-id="${child.id}">
                            ${child.categoryName}
                        </li>`).join('')
                        : '';

                    const submenu = hasChildren
                        ? `<ul class="dropdown-submenu">${subItems}</ul>`
                        : '';

                    const arrow = hasChildren ? `<span class="arrow">▶</span>` : '';

                    return `
                    <li class="dropdown-item" data-category-id="${parent.id}">
                        ${parent.categoryName}
                        ${arrow}
                        ${submenu}
                    </li>`;
                }).join('');

                // 统一用事件委托处理点击，点击时动态读取当前URL参数
                dropdown.addEventListener('click', function (e) {
                    const target = e.target.closest('[data-category-id]');
                    if (!target) return;
                    // 判断子父分类项
                    const isSubItem = target.classList.contains('dropdown-subitem');
                    const isParentItem = target.classList.contains('dropdown-item');

                    if (!isSubItem && !isParentItem) return;

                    //子分类直接用自身的categoryId跳转
                    if (isSubItem) {
                        const categoryId = target.dataset.categoryId;
                        const params = new URLSearchParams(window.location.search);
                        params.set('categoryId', categoryId);
                        location.href = '/pages/front/index.html?' + params.toString();
                        return;
                    }

                    //父分类确保点击的不是箭头展开区域以外的子菜单触发
                    // 点到.dropdown-item就跳转
                    if (isParentItem && !e.target.closest('.dropdown-submenu')) {
                        const categoryId = target.dataset.categoryId;
                        const params = new URLSearchParams(window.location.search);
                        params.set('categoryId', categoryId);
                        location.href = '/pages/front/index.html?' + params.toString();
                    }
                });
            });
    };
    //初始化标签菜单
    NanoBlog.initTagMenu = function () {
        const container = document.getElementById('tagDropdown');
        if (!container) return;

        fetch('/tag/list')
            .then(res => res.json())
            .then(result => {
                if (result.code !== 200 || !result.data) return;
                const tags = result.data.slice(0, 15);
                container.innerHTML = tags.map(tag => `
                <a class="tag-cloud-item"
                   data-tag-id="${tag.id}"
                   style="${tag.tagColor ? 'border-color:' + tag.tagColor : ''}">
                    ${tag.tagName}
                    <span class="tag-count">${tag.articleCount || 0}</span>
                </a>
            `).join('');

                // 点击时动态读取当前URL参数
                container.addEventListener('click', function (e) {
                    const target = e.target.closest('[data-tag-id]');
                    if (!target) return;
                    const tagId = target.dataset.tagId;
                    const params = new URLSearchParams(window.location.search);
                    params.set('tagId', tagId);
                    location.href = '/pages/front/index.html?' + params.toString();
                });
            });
    };
    // 工具函数构建筛选跳转URL，保留已有参数并合并新参数
    NanoBlog.buildFilterUrl = function (newParams) {
        const params = new URLSearchParams(window.location.search);
        Object.entries(newParams).forEach(([key, value]) => {
            if (value !== null && value !== undefined) {
                params.set(key, value);
            } else {
                params.delete(key);
            }
        });
        return '/pages/front/index.html?' + params.toString();
    };

    // 页面初始化入口
    NanoBlog.init = function () {
        NanoBlog.setActiveNav();
        NanoBlog.bindNavbarScrollEffect();
        NanoBlog.initNavUser();
        NanoBlog.initCategoryMenu();
        NanoBlog.initTagMenu();
    };

    window.NanoBlog = NanoBlog;

    document.addEventListener('DOMContentLoaded', NanoBlog.init);
})(window, document);