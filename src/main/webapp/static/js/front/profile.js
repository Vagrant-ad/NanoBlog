/* ================================================================
     工具函数
  ================================================================ */
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
    }
}

/* ================================================================
   原有功能（未做任何修改）
================================================================ */

window.onload = function () {
    fetchProfile();
    fetchStats();
    loadMyArticles();
};

/* 1. 获取并渲染资料 —— 原始逻辑 */
function fetchProfile() {
    fetch('/user/getProfile')
        .then(res => res.json())
        .then(res => {
            if (res.code === 200) {
                const u  = res.data.user;
                const rId = res.data.roleId;

                document.getElementById('userId').value           = u.id;
                document.getElementById('nicknameDisplay').innerText = u.nickname || '未设置昵称';
                document.getElementById('usernameDisplay').innerText = '@' + u.username;
                document.getElementById('emailDisplay').innerText  = u.email || '未绑定';
                document.getElementById('bioDisplay').innerText    = u.bio || '这个人很懒，暂时没有简介。';

                document.getElementById('nicknameInput').value = u.nickname || '';
                document.getElementById('emailInput').value    = u.email    || '';
                document.getElementById('bioInput').value      = u.bio      || '';

                const roleEl = document.getElementById('roleDisplay');
                if (rId == 2) {
                    roleEl.innerText    = '管理员';
                    roleEl.style.color  = '#e53e3e';
                } else {
                    roleEl.innerText    = '普通用户';
                    roleEl.style.color  = '#4a5568';
                }

                const formatTime = (t) => t ? t.replace('T', ' ').split('.')[0] : '---';
                document.getElementById('createTimeDisplay').innerText  = formatTime(u.createTime);
                document.getElementById('lastLoginDisplay').innerText   = formatTime(u.lastLoginTime);
                document.getElementById('updateTimeDisplay').innerText  = formatTime(u.updateTime);

                if (u.avatarUrl) document.getElementById('avatarDisplay').src = u.avatarUrl;

                const badge = document.getElementById('statusBadge');
                badge.innerText   = u.status === 1 ? '正常' : '已封禁';
                badge.className   = u.status === 1 ? 'status-badge status-ok' : 'status-badge status-error';
            } else {
                window.location.href = 'login.html';
            }
        })
        .catch(err => console.error('加载个人资料出错:', err));
}

/* 2. 开启编辑模式 —— 原始逻辑 */
function enableEdit() {
    document.getElementById('viewPanel').style.display = 'none';
    document.getElementById('editPanel').style.display = 'block';
    document.getElementById('editBtn').style.display   = 'none';
}

/* 3. 取消编辑模式 —— 原始逻辑 */
function cancelEdit() {
    document.getElementById('viewPanel').style.display = 'block';
    document.getElementById('editPanel').style.display = 'none';
    document.getElementById('editBtn').style.display   = 'flex';
}

/* 4. 提交更新请求 —— 原始逻辑（alert → showToast） */
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

/* 5. 头像上传 —— 原始逻辑（alert → showToast） */
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

/* ================================================================
   新增功能
================================================================ */

/* ── 统计数据（接口预留）──
   TODO（队友对接）：GET /user/getStats
   响应：{ code:200, data:{ followCount, viewCount, likeCount } }
*/
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
        .catch(() => { /* 接口未就绪时静默失败，保持 -- 占位 */ });
}

function formatNumber(n) {
    if (n === undefined || n === null) return '--';
    if (n >= 10000) return (n / 10000).toFixed(1) + 'w';
    if (n >= 1000)  return (n / 1000).toFixed(1) + 'k';
    return n;
}

/* ── 我的文章（接口预留）──
   TODO（队友对接）：GET /article/getMyArticles?page=1&size=5
   响应：{ code:200, data:{ list:[{ id, title, coverUrl, viewCount, likeCount, status, createTime }], total } }
*/
function loadMyArticles() {
    fetch('/article/getMyArticles?page=1&size=5')
        .then(res => res.json())
        .then(res => {
            if (res.code === 200 && res.data.list && res.data.list.length > 0) {
                renderArticleList(res.data.list);
            }
            // 若接口未就绪或无文章，保留默认空状态占位
        })
        .catch(() => { /* 接口未就绪时静默失败 */ });
}

function renderArticleList(list) {
    const container = document.getElementById('articleList');
    container.innerHTML = list.map(a => `
            <div class="article-item" onclick="goToArticle(${a.id})">
                <div class="article-thumb">
                    ${a.coverUrl
        ? `<img src="${a.coverUrl}" alt="${a.title}">`
        : '<i class="fas fa-file-alt"></i>'}
                </div>
                <div class="article-info">
                    <div class="article-title">${a.title}</div>
                    <div class="article-meta">
                        <span><i class="fas fa-eye"></i> ${formatNumber(a.viewCount)}</span>
                        <span><i class="fas fa-thumbs-up"></i> ${formatNumber(a.likeCount)}</span>
                        <span><i class="fas fa-clock"></i> ${formatArticleTime(a.createTime)}</span>
                    </div>
                </div>
                <span class="article-status ${a.status === 1 ? 'status-published' : 'status-draft'}">
                    ${a.status === 1 ? '已发布' : '草稿'}
                </span>
            </div>
        `).join('');
}

function formatArticleTime(t) {
    if (!t) return '---';
    return t.replace('T', ' ').split('.')[0].substring(0, 10);
}

/* TODO（队友对接）跳转文章详情 */
function goToArticle(id) {
    window.location.href = `/pages/front/article.html?id=${id}`;
}

/* TODO（队友对接）跳转我的全部文章列表 */
function goToMyArticles() {
    window.location.href = '/pages/front/my-articles.html';
}

/* ── 修改密码 Modal ── */
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

/* TODO（队友对接）：POST /user/changePassword
   Body：{ oldPassword, newPassword }
   响应：{ code:200 } → 成功，{ code:400, msg:"原密码错误" } → 失败
*/
function submitChangePassword() {
    const oldPwd  = document.getElementById('oldPassword').value.trim();
    const newPwd  = document.getElementById('newPassword').value.trim();
    const confPwd = document.getElementById('confirmPassword').value.trim();

    if (!oldPwd || !newPwd || !confPwd) {
        showToast('请填写所有密码字段', 'error'); return;
    }
    if (newPwd !== confPwd) {
        showToast('两次输入的新密码不一致', 'error'); return;
    }
    if (newPwd.length < 6) {
        showToast('新密码长度不能少于 6 位', 'error'); return;
    }

    //发送请求
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

/* 密码强度检测 */
function checkPwdStrength(val) {
    const bars  = [1,2,3,4].map(i => document.getElementById('pBar' + i));
    const label = document.getElementById('pLabel');
    bars.forEach(b => b.className = 'pwd-bar');

    if (!val) { label.innerText = ''; return; }

    let score = 0;
    if (val.length >= 8)  score++;
    if (/[A-Z]/.test(val)) score++;
    if (/[0-9]/.test(val)) score++;
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

/* ── 注销账号 Modal ── */
function openDeleteConfirm() {
    document.getElementById('deleteConfirmPassword').value = '';
    document.getElementById('deleteModal').classList.add('active');
}

function closeDeleteConfirm() {
    document.getElementById('deleteModal').classList.remove('active');
}

/* TODO（队友对接）：POST /user/deleteAccount
   Body：{ password }
   响应：{ code:200 } → 清 session 后跳转登录页
*/
function submitDeleteAccount() {
    const pwd = document.getElementById('deleteConfirmPassword').value.trim();
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