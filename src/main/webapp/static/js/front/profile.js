/* 工具函数 */
function showToast(msg, type = 'info') {
    const t = document.getElementById('toast');
    const icons = { success: 'fa-check-circle', error: 'fa-times-circle', info: 'fa-info-circle' };
    t.innerHTML = `<i class="fas ${icons[type]}"></i> ${msg}`;
    t.className = `toast ${type} show`;
    setTimeout(() => { t.className = 'toast'; }, 3000);
}

function toggleEye(inputId, btn) {
    const input = document.getElementById(inputId);
    const icon  = btn.querySelector('i');
    if (input.type === 'password') {
        input.type = 'text';
        icon.className = 'fas fa-eye';
    } else {
        input.type = 'password';
        icon.className = 'fas fa-eye-slash';
    }
}

function handleOverlayClick(e, modalId) {
    if (e.target.id === modalId) {
        if (modalId === 'pwdModal') closeChangePassword();
        if (modalId === 'deleteModal') closeDeleteConfirm();
        if (modalId === 'editArticleModal') closeEditArticle();
    }
}

function formatNumber(n) {
    if (n === undefined || n === null) return '--';
    if (n >= 10000) return (n / 10000).toFixed(1) + 'w';
    if (n >= 1000)  return (n / 1000).toFixed(1) + 'k';
    return n;
}

function formatArticleTime(t) {
    if (!t) return '---';
    return String(t).replace('T', ' ').split('.')[0].substring(0, 16);
}

/* 页面初始化*/
window.onload = function () {
    fetchProfile();
    fetchStats();
    loadPublishedArticles(1);
    loadDraftArticles(1);
};

/* 个人资料*/
function fetchProfile() {
    fetch('/user/getProfile')
        .then(res => res.json())
        .then(res => {
            if (res.code === 200) {
                const u  = res.data.user;
                const rId = res.data.roleId;

                document.getElementById('userId').value              = u.id;
                document.getElementById('nicknameDisplay').innerText = u.nickname || '未设置昵称';
                document.getElementById('usernameDisplay').innerText = '@' + u.username;
                document.getElementById('emailDisplay').innerText    = u.email || '未绑定';
                document.getElementById('bioDisplay').innerText      = u.bio || '这个人很懒，暂时没有简介。';

                document.getElementById('nicknameInput').value = u.nickname || '';
                document.getElementById('emailInput').value    = u.email    || '';
                document.getElementById('bioInput').value      = u.bio      || '';

                const roleEl = document.getElementById('roleDisplay');
                if (rId == 2) {
                    roleEl.innerText   = '管理员';
                    roleEl.style.color = '#e53e3e';
                } else {
                    roleEl.innerText   = '普通用户';
                    roleEl.style.color = '#4a5568';
                }

                const fmt = (t) => t ? String(t).replace('T', ' ').split('.')[0] : '---';
                document.getElementById('createTimeDisplay').innerText  = fmt(u.createTime);
                document.getElementById('lastLoginDisplay').innerText   = fmt(u.lastLoginTime);
                document.getElementById('updateTimeDisplay').innerText  = fmt(u.updateTime);

                if (u.avatarUrl) document.getElementById('avatarDisplay').src = u.avatarUrl;

                const badge = document.getElementById('statusBadge');
                badge.innerText = u.status === 1 ? '正常' : '已封禁';
                badge.className = u.status === 1 ? 'status-badge status-ok' : 'status-badge status-error';
            } else {
                window.location.href = 'login.html';
            }
        })
        .catch(err => console.error('加载个人资料出错:', err));
}

function enableEdit() {
    document.getElementById('viewPanel').style.display = 'none';
    document.getElementById('editPanel').style.display = 'block';
    document.getElementById('editBtn').style.display   = 'none';
}

function cancelEdit() {
    document.getElementById('viewPanel').style.display = 'block';
    document.getElementById('editPanel').style.display = 'none';
    document.getElementById('editBtn').style.display   = 'flex';
}

function submitUpdate() {
    const updateData = {
        id:        document.getElementById('userId').value,
        nickname:  document.getElementById('nicknameInput').value,
        email:     document.getElementById('emailInput').value,
        bio:       document.getElementById('bioInput').value,
        avatarUrl: document.getElementById('avatarDisplay').src
    };
    fetch('/user/updateProfile', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updateData)
    })
        .then(res => res.json())
        .then(res => {
            if (res.code === 200) {
                showToast('资料修改成功！', 'success');
                setTimeout(() => location.reload(), 1200);
            } else {
                showToast('更新失败：' + res.msg, 'error');
            }
        });
}

function uploadAvatar(input) {
    if (!input.files || !input.files[0]) return;
    const formData = new FormData();
    formData.append('file', input.files[0]);
    fetch('/user/uploadAvatar', { method: 'POST', body: formData })
        .then(res => res.json())
        .then(res => {
            if (res.code === 200) {
                document.getElementById('avatarDisplay').src = res.data;
                showToast('预览头像已更新，点击【保存修改】生效', 'info');
            } else {
                showToast('上传失败：' + res.msg, 'error');
            }
        });
}

/*  统计数据 */
function fetchStats() {
    fetch('/user/getStats')
        .then(res => res.json())
        .then(res => {
            if (res.code === 200) {
                document.getElementById('statFollow').innerText = formatNumber(res.data.followCount);
                document.getElementById('statView').innerText   = formatNumber(res.data.viewCount);
                document.getElementById('statLike').innerText   = formatNumber(res.data.likeCount);
            }
        })
        .catch(() => {});
}

/* 文章管理 - Tab 切换 */
function switchTab(tab) {
    document.querySelectorAll('.article-tab').forEach(btn => btn.classList.remove('active'));
    document.querySelector(`.article-tab[data-tab="${tab}"]`).classList.add('active');
    document.getElementById('tabPublished').style.display = tab === 'published' ? 'block' : 'none';
    document.getElementById('tabDrafts').style.display    = tab === 'drafts'    ? 'block' : 'none';
}

/*已发布文章 */
let publishedPage = 1;
const publishedPageSize = 5;

function loadPublishedArticles(page) {
    publishedPage = page;
    const container = document.getElementById('publishedList');
    container.innerHTML = '<div class="articles-loading"><i class="fas fa-spinner fa-spin"></i> 加载中...</div>';

    fetch(`/article/my/published?page=${page}&size=${publishedPageSize}`)
        .then(res => res.json())
        .then(res => {
            if (res.code === 200 && res.data) {
                const { records, total } = res.data;
                document.getElementById('publishedCount').innerText = total || 0;
                renderArticleItems(container, records, false);
                renderPagination('publishedPagination', total, page, publishedPageSize, loadPublishedArticles);
            } else {
                renderEmpty(container, '暂无已发布文章');
                document.getElementById('publishedCount').innerText = 0;
            }
        })
        .catch(() => renderEmpty(container, '加载失败，请刷新重试'));
}

/*草稿列表*/
let draftsPage = 1;
const draftsPageSize = 5;

function loadDraftArticles(page) {
    draftsPage = page;
    const container = document.getElementById('draftsList');
    container.innerHTML = '<div class="articles-loading"><i class="fas fa-spinner fa-spin"></i> 加载中...</div>';

    fetch(`/article/my/drafts?page=${page}&size=${draftsPageSize}`)
        .then(res => res.json())
        .then(res => {
            if (res.code === 200 && res.data) {
                const { records, total } = res.data;
                document.getElementById('draftsCount').innerText = total || 0;
                renderArticleItems(container, records, true);
                renderPagination('draftsPagination', total, page, draftsPageSize, loadDraftArticles);
            } else {
                renderEmpty(container, '草稿箱是空的');
                document.getElementById('draftsCount').innerText = 0;
            }
        })
        .catch(() => renderEmpty(container, '加载失败，请刷新重试'));
}

/*  渲染文章列表项*/
function renderArticleItems(container, records, isDraft) {
    if (!records || records.length === 0) {
        renderEmpty(container, isDraft ? '草稿箱是空的' : '暂无已发布文章');
        return;
    }

    const DEFAULT_COVER = '/static/images/demo-cover.jpg';
    const html = records.map(a => {
        const title    = escapeHtml(a.articleTitle || '未命名文章');
        const summary  = escapeHtml(a.articleSummary || '');
        const tags     = Array.isArray(a.tags) ? a.tags : [];
        const tagsHtml = tags.length
            ? tags.map(t => `<span class="article-manage-tag">${escapeHtml(t)}</span>`).join('')
            : '';

        const thumbHtml = a.coverImageUrl
            ? `<img src="${a.coverImageUrl}" alt="${title}" onerror="this.parentElement.innerHTML='<i class=\\'fas fa-file-alt\\'></i>'">`
            : '<i class="fas fa-file-alt"></i>';

        const publishDraftBtn = isDraft
            ? `<button class="article-action-btn btn-publish-draft" onclick="publishDraftArticle(${a.id})">
                   <i class="fas fa-paper-plane"></i> 发布
               </button>`
            : '';

        const timeLabel = isDraft ? '创建' : '发布';
        const timeVal   = isDraft
            ? formatArticleTime(a.createTime)
            : formatArticleTime(a.publishTime);

        return `
            <div class="article-manage-item">
                <div class="article-manage-thumb">${thumbHtml}</div>
                <div class="article-manage-info" onclick="goToArticleDetail(${a.id})">
                    <div class="article-manage-title">${title}</div>
                    ${summary ? `<div class="article-manage-summary">${summary}</div>` : ''}
                    ${tagsHtml ? `<div class="article-manage-tags">${tagsHtml}</div>` : ''}
                    <div class="article-manage-meta">
                        <span><i class="fas fa-clock"></i> ${timeLabel}: ${timeVal}</span>
                        <span><i class="fas fa-eye"></i> ${formatNumber(a.viewCount)}</span>
                        <span><i class="fas fa-thumbs-up"></i> ${formatNumber(a.likeCount)}</span>
                        <span><i class="fas fa-comment"></i> ${formatNumber(a.commentCount)}</span>
                    </div>
                </div>
                <div class="article-manage-actions">
                    ${publishDraftBtn}
                    <button class="article-action-btn btn-edit" onclick="openEditArticle(${a.id})">
                        <i class="fas fa-edit"></i> 编辑
                    </button>
                    <button class="article-action-btn btn-delete" onclick="confirmDeleteArticle(${a.id}, '${title}')">
                        <i class="fas fa-trash-alt"></i> 删除
                    </button>
                </div>
            </div>
        `;
    }).join('');

    container.innerHTML = html;
}

function renderEmpty(container, msg) {
    container.innerHTML = `
        <div class="articles-empty">
            <i class="fas fa-file-alt"></i>
            <span>${msg}</span>
        </div>`;
}

function renderPagination(elId, total, currentPage, pageSize, callback) {
    const el = document.getElementById(elId);
    if (!el) return;
    const totalPages = Math.ceil(total / pageSize);
    if (totalPages <= 1) { el.innerHTML = ''; return; }

    let btns = '';
    btns += `<button class="page-btn" onclick="${callback.name}(${currentPage - 1})" ${currentPage <= 1 ? 'disabled' : ''}>‹</button>`;

    const start = Math.max(1, currentPage - 2);
    const end   = Math.min(totalPages, currentPage + 2);
    for (let i = start; i <= end; i++) {
        btns += `<button class="page-btn ${i === currentPage ? 'active' : ''}" onclick="${callback.name}(${i})">${i}</button>`;
    }

    btns += `<button class="page-btn" onclick="${callback.name}(${currentPage + 1})" ${currentPage >= totalPages ? 'disabled' : ''}>›</button>`;
    el.innerHTML = btns;
}

/*  文章操作*/
function goToArticleDetail(id) {
    window.open(`/pages/front/post.html?id=${id}`, '_blank');
}

// ── 删除文章 ──
let _pendingDeleteId = null;

function confirmDeleteArticle(id, title) {
    _pendingDeleteId = id;
    // 复用已有的 deleteModal，替换文案
    const body = document.querySelector('#deleteModal .confirm-body');
    if (body) {
        body.innerHTML = `
            <div class="confirm-icon"><i class="fas fa-trash-alt"></i></div>
            <h4>确认删除此文章？</h4>
            <p>「${title}」将被<strong>永久删除</strong>，无法找回。</p>
        `;
    }
    document.getElementById('deleteModal').classList.add('active');
    // 替换确认按钮行为
    const confirmBtn = document.querySelector('#deleteModal .btn-danger');
    if (confirmBtn) {
        confirmBtn.onclick = doDeleteArticle;
    }
}

function doDeleteArticle() {
    if (!_pendingDeleteId) return;
    fetch(`/article/${_pendingDeleteId}`, { method: 'DELETE' })
        .then(res => res.json())
        .then(res => {
            closeDeleteConfirm();
            if (res.code === 200) {
                showToast('文章已删除', 'success');
                loadPublishedArticles(publishedPage);
                loadDraftArticles(draftsPage);
            } else {
                showToast(res.msg || '删除失败', 'error');
            }
        })
        .catch(() => showToast('网络异常，请稍后再试', 'error'));
}

// ── 发布草稿 ──
function publishDraftArticle(id) {
    if (!confirm('确认将此草稿发布？')) return;
    fetch(`/article/${id}/publish`, { method: 'POST' })
        .then(res => res.json())
        .then(res => {
            if (res.code === 200) {
                showToast('草稿发布成功！', 'success');
                loadPublishedArticles(1);
                loadDraftArticles(1);
            } else {
                showToast(res.msg || '发布失败', 'error');
            }
        })
        .catch(() => showToast('网络异常，请稍后再试', 'error'));
}

// ── 编辑文章 ──
let _editingArticleId = null;

function openEditArticle(id) {
    if (!id) {
        showToast('文章ID缺失', 'error');
        return;
    }
    // 携带文章id跳转到文章界面
    window.location.href = `/pages/front/editor.html?id=${id}`;
}

function fillEditForm(data) {
    document.getElementById('editTitle').value      = data.title || '';
    document.getElementById('editSummary').value    = data.articleSummary || '';
    document.getElementById('editCoverUrl').value   = data.coverImageUrl || '';
    document.getElementById('editContentMd').value  = data.contentMd || '';
    document.getElementById('editStatus').value     = String(data.status ?? 1);
    // 标签
    const tags = Array.isArray(data.tags) ? data.tags.join(', ') : '';
    document.getElementById('editTags').value = tags;
    // 分类：先加载分类列表
    loadCategoriesForEdit(data.categoryId);
}

function loadCategoriesForEdit(selectedId) {
    fetch('/category/list')
        .then(res => res.json())
        .then(res => {
            const sel = document.getElementById('editCategory');
            sel.innerHTML = '<option value="">请选择分类</option>';
            if (res.code === 200 && res.data) {
                res.data.forEach(c => {
                    const opt = document.createElement('option');
                    opt.value = c.id;
                    opt.textContent = c.categoryName;
                    if (c.id == selectedId) opt.selected = true;
                    sel.appendChild(opt);
                });
            }
        })
        .catch(() => {});
}

function closeEditArticle() {
    document.getElementById('editArticleModal').classList.remove('active');
    _editingArticleId = null;
}



function submitEditArticle() {
    if (!_editingArticleId) return;

    const tagsRaw = document.getElementById('editTags').value;
    const tags = tagsRaw.split(',').map(t => t.trim()).filter(Boolean);

    const payload = {
        articleTitle:    document.getElementById('editTitle').value.trim(),
        articleSummary:  document.getElementById('editSummary').value.trim(),
        categoryId:      document.getElementById('editCategory').value || null,
        coverUrl:        document.getElementById('editCoverUrl').value.trim(),
        contentMd:       document.getElementById('editContentMd').value,
        status:          parseInt(document.getElementById('editStatus').value),
        tags:            tags
    };

    if (!payload.articleTitle) { showToast('标题不能为空', 'error'); return; }

    fetch(`/article/${_editingArticleId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
        .then(res => res.json())
        .then(res => {
            if (res.code === 200) {
                showToast('文章更新成功！', 'success');
                closeEditArticle();
                loadPublishedArticles(publishedPage);
                loadDraftArticles(draftsPage);
            } else {
                showToast(res.msg || '更新失败', 'error');
            }
        })
        .catch(() => showToast('网络异常，请稍后再试', 'error'));
}

/* 修改密码 Modal*/
function openChangePassword() {
    document.getElementById('oldPassword').value     = '';
    document.getElementById('newPassword').value     = '';
    document.getElementById('confirmPassword').value = '';
    resetPwdStrength();
    document.getElementById('pwdModal').classList.add('active');
}

function closeChangePassword() {
    document.getElementById('pwdModal').classList.remove('active');
}

function submitChangePassword() {
    const oldPwd  = document.getElementById('oldPassword').value.trim();
    const newPwd  = document.getElementById('newPassword').value.trim();
    const confPwd = document.getElementById('confirmPassword').value.trim();

    if (!oldPwd || !newPwd || !confPwd) { showToast('请填写所有密码字段', 'error'); return; }
    if (newPwd !== confPwd)             { showToast('两次输入的新密码不一致', 'error'); return; }
    if (newPwd.length < 6)              { showToast('新密码长度不能少于 6 位', 'error'); return; }

    fetch('/user/updatePassword', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ oldPassword: oldPwd, newPassword: newPwd })
    })
        .then(res => res.json())
        .then(res => {
            if (res.code === 200) {
                showToast('密码修改成功，请重新登录', 'success');
                closeChangePassword();
                setTimeout(() => window.location.href = 'login.html', 1500);
            } else {
                showToast(res.msg || '修改失败', 'error');
            }
        })
        .catch(() => showToast('网络异常，请稍后再试', 'error'));
}

function checkPwdStrength(val) {
    const bars  = [1,2,3,4].map(i => document.getElementById('pBar' + i));
    const label = document.getElementById('pLabel');
    bars.forEach(b => b.className = 'pwd-bar');
    if (!val) { label.innerText = ''; return; }
    let score = 0;
    if (val.length >= 8)          score++;
    if (/[A-Z]/.test(val))        score++;
    if (/[0-9]/.test(val))        score++;
    if (/[^A-Za-z0-9]/.test(val)) score++;
    const levels = ['', 'weak', 'fair', 'good', 'strong'];
    const names  = ['', '弱', '中', '良', '强'];
    for (let i = 0; i < score; i++) bars[i].classList.add(levels[score]);
    label.innerText = names[score] || '';
}

function resetPwdStrength() {
    [1,2,3,4].forEach(i => document.getElementById('pBar' + i).className = 'pwd-bar');
    document.getElementById('pLabel').innerText = '';
}

/*  注销账号 Modal */
function openDeleteConfirm() {
    // 重置为注销账号的原始内容
    const body = document.querySelector('#deleteModal .confirm-body');
    if (body) {
        body.innerHTML = `
            <div class="confirm-icon"><i class="fas fa-trash-alt"></i></div>
            <h4>确认注销账号？</h4>
            <p>你的所有文章、评论、关注数据将被<strong>永久删除</strong>，无法找回。请输入密码确认此操作。</p>
            <div class="modal-field" style="margin-top:16px; text-align:left;">
                <label style="font-size:0.72rem;font-weight:700;color:#dc2626;letter-spacing:0.5px;text-transform:uppercase;display:block;margin-bottom:6px;">验证当前密码</label>
                <div class="modal-field-wrap">
                    <input type="password" id="deleteConfirmPassword" placeholder="输入密码以确认">
                </div>
            </div>
        `;
    }
    const confirmBtn = document.querySelector('#deleteModal .btn-danger');
    if (confirmBtn) confirmBtn.onclick = submitDeleteAccount;
    document.getElementById('deleteModal').classList.add('active');
}

function closeDeleteConfirm() {
    document.getElementById('deleteModal').classList.remove('active');
    _pendingDeleteId = null;
}

function submitDeleteAccount() {
    const pwd = document.getElementById('deleteConfirmPassword')?.value?.trim();
    if (!pwd) { showToast('请输入密码以确认操作', 'error'); return; }
    fetch('/user/deleteAccount', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ password: pwd })
    })
        .then(res => res.json())
        .then(res => {
            if (res.code === 200) {
                showToast('账号已注销，再见…', 'info');
                setTimeout(() => window.location.href = 'login.html', 1800);
            } else {
                showToast(res.msg || '注销失败，密码可能有误', 'error');
            }
        })
        .catch(() => showToast('网络异常，请稍后再试', 'error'));
}

/* XSS 防护*/
function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}