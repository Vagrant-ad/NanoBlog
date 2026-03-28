(function (window, document) {
    'use strict';

    const NanoBlog = {
        apiBase: ''
    };

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

    NanoBlog.debounce = function (fn, delay = 300) {
        let timer = null;
        return function (...args) {
            clearTimeout(timer);
            timer = setTimeout(() => fn.apply(this, args), delay);
        };
    };

    NanoBlog.getQueryParam = function (name) {
        const params = new URLSearchParams(window.location.search);
        return params.get(name);
    };

    NanoBlog.escapeHtml = function (str) {
        if (str === null || str === undefined) return '';
        return String(str)
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
    };

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

    NanoBlog.truncateText = function (text, maxLen = 120) {
        if (!text) return '';
        const str = String(text).trim();
        if (str.length <= maxLen) return str;
        return str.slice(0, maxLen).trimEnd() + '...';
    };

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

    NanoBlog.toast = function (msg, icon = 2) {
        if (window.layui && layui.layer) {
            layui.layer.msg(msg, { icon, time: 1800 });
        } else {
            alert(msg);
        }
    };

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

    NanoBlog.init = function () {
        NanoBlog.setActiveNav();
        NanoBlog.bindNavbarScrollEffect();
    };

    window.NanoBlog = NanoBlog;

    document.addEventListener('DOMContentLoaded', NanoBlog.init);
})(window, document);