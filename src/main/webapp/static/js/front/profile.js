/* 全局状态*/
var _profileUserId  = null;
var _currentLoginId = null;
var _isOwner        = false;
var _isFollowing    = false; // 提取为全局变量，专门管理关注状态

/* 工具函数 */
function showToast(msg, type) {
    type = type || 'info';
    var t = document.getElementById('toast');
    if (!t) return;
    var icons = { success: 'fa-check-circle', error: 'fa-times-circle', info: 'fa-info-circle' };
    t.innerHTML = '<i class="fas ' + icons[type] + '"></i> ' + msg;
    t.className = 'toast ' + type + ' show';
    setTimeout(function () { t.className = 'toast'; }, 3000);
}

function toggleEye(inputId, btn) {
    var input = document.getElementById(inputId);
    var icon  = btn.querySelector('i');
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
        if (modalId === 'pwdModal')          closeChangePassword();
        if (modalId === 'deleteModal')       closeDeleteConfirm();
        if (modalId === 'editArticleModal')  closeEditArticle();
        if (modalId === 'publishDraftModal') closePublishDraftModal();
    }
}

function formatNumber(n) {
    if (n === undefined || n === null) return '--';
    if (n >= 10000) return (n / 10000).toFixed(1) + 'w';
    if (n >= 1000)  return (n / 1000).toFixed(1) + 'k';
    return String(n);
}

function formatArticleTime(t) {
    if (!t) return '---';
    return String(t).replace('T', ' ').split('.')[0].substring(0, 16);
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function resolveApiBase() {
    if (window.NanoBlog && typeof window.NanoBlog.apiBase === 'string') {
        return window.NanoBlog.apiBase;
    }
    var path = window.location.pathname || '';
    var idx = path.indexOf('/pages/');
    return idx > 0 ? path.substring(0, idx) : '';
}

var API_BASE = resolveApiBase();

function getQueryParam(name) {
    return new URLSearchParams(window.location.search).get(name);
}

/* 打开用户已注销弹窗 */
function openUserDeletedModal() {
    var modal = document.getElementById('userDeletedModal');
    if (modal) modal.classList.add('active');
}

/* 页面初始化 */
window.onload = function () {
    var urlId = getQueryParam('id');

    // 1. 获取当前登录用户
    fetch(API_BASE + '/user/getProfile', { credentials: 'same-origin' })
        .then(function (r) { return r.json(); })
        .then(function (res) {
            if (res.code === 200 && res.data && res.data.user) {
                _currentLoginId = String(res.data.user.id);
            }
        })
        .catch(function (err) { console.warn("未登录或获取状态失败", err); })
        .finally(function () {
            // 2. 确认被访问的主页属于谁
            if (urlId) {
                _profileUserId = urlId;
                _isOwner = (_currentLoginId !== null && _currentLoginId === String(urlId));
            } else {
                if (_currentLoginId) {
                    _profileUserId = _currentLoginId;
                    _isOwner = true;
                    history.replaceState(null, '', window.location.pathname + '?id=' + _currentLoginId);
                } else {
                    window.location.href = API_BASE + '/pages/front/login.html';
                    return;
                }
            }

            // 3. 根据是否是访客进行页面渲染
            if (!_isOwner && _profileUserId) {
                checkUserDeletedThenInit(_profileUserId);
            } else {
                initPageByMode();
            }
        });
};

function checkUserDeletedThenInit(userId) {
    fetch(API_BASE + '/user/publicProfile?id=' + userId, { credentials: 'same-origin' })
        .then(function (r) { return r.json(); })
        .then(function (res) {
            if (res.code === 200 && res.data) {
                initPageByMode();
            } else {
                initPageByModeDeleted();
            }
        })
        .catch(function () {
            initPageByModeDeleted();
        });
}

function initPageByModeDeleted() {
    document.getElementById('ownerActions').style.display   = 'none';
    document.getElementById('visitorActions').style.display = 'none';
    document.getElementById('avatarMaskLabel').style.display = 'none';
    document.getElementById('draftsTabBtn').style.display   = 'none';
    document.getElementById('dangerZoneCard').style.display = 'none';
    document.getElementById('articleCardTitle').innerText   = 'TA 的文章';
    renderDeletedUserSkeleton();
    openUserDeletedModal();
}

function renderDeletedUserSkeleton() {
    var nicknameEl = document.getElementById('nicknameDisplay');
    var usernameEl = document.getElementById('usernameDisplay');
    var statusEl   = document.getElementById('statusBadge');
    var bioEl      = document.getElementById('bioDisplay');
    var emailEl    = document.getElementById('emailDisplay');
    var roleEl     = document.getElementById('roleDisplay');
    var createEl   = document.getElementById('createTimeDisplay');
    var loginEl    = document.getElementById('lastLoginDisplay');
    var updateEl   = document.getElementById('updateTimeDisplay');
    var userIdEl   = document.getElementById('userId');

    if (nicknameEl) nicknameEl.innerText = '已注销用户';
    if (usernameEl) usernameEl.innerText = '@unknown';
    if (statusEl) {
        statusEl.innerText = '已注销';
        statusEl.className = 'status-badge status-error';
    }
    if (bioEl)    bioEl.innerText    = '该账号已注销。';
    if (emailEl)  emailEl.innerText  = '---';
    if (roleEl)   roleEl.innerText   = '---';
    if (createEl) createEl.innerText = '---';
    if (loginEl)  loginEl.innerText  = '---';
    if (updateEl) updateEl.innerText = '---';
    if (userIdEl) userIdEl.value     = _profileUserId || '';

    var statFollow  = document.getElementById('statFollow');
    var statFans    = document.getElementById('statFans');
    var statView    = document.getElementById('statView');
    var statLike    = document.getElementById('statLike');
    var statComment = document.getElementById('statComment');
    if (statFollow)  statFollow.innerText  = '--';
    if (statFans)    statFans.innerText    = '--';
    if (statView)    statView.innerText    = '--';
    if (statLike)    statLike.innerText    = '--';
    if (statComment) statComment.innerText = '--';

    var publishedList = document.getElementById('publishedList');
    if (publishedList) {
        publishedList.innerHTML =
            '<div class="articles-empty"><i class="fas fa-file-alt"></i><span>暂无内容</span></div>';
    }
    var publishedCount = document.getElementById('publishedCount');
    if (publishedCount) publishedCount.innerText = 0;
}

/* 根据 owner/visitor 模式初始化页面 */
function initPageByMode() {
    if (_isOwner) {
        document.getElementById('ownerActions').style.display   = 'flex';
        document.getElementById('visitorActions').style.display = 'none';
        document.getElementById('avatarMaskLabel').style.display = '';
        document.getElementById('draftsTabBtn').style.display   = '';
        document.getElementById('dangerZoneCard').style.display = '';
        document.getElementById('articleCardTitle').innerText   = '文章管理';
        fetchProfileOwner();
        fetchStats(_profileUserId);
        loadPublishedArticles(1);
        loadDraftArticles(1);
    } else {
        document.getElementById('ownerActions').style.display   = 'none';
        document.getElementById('visitorActions').style.display = 'flex';
        document.getElementById('avatarMaskLabel').style.display = 'none';
        document.getElementById('draftsTabBtn').style.display   = 'none';
        document.getElementById('dangerZoneCard').style.display = 'none';
        document.getElementById('articleCardTitle').innerText   = 'TA 的文章';

        //  visitor 模式：隐藏整个 Tab 栏
        var tabsBar = document.getElementById('articleTabsBar');
        if (tabsBar) tabsBar.style.display = 'none';
        // 直接显示已发布文章区域
        var tabPublished = document.getElementById('tabPublished');
        if (tabPublished) tabPublished.style.display = 'block';

        fetchProfileVisitor(_profileUserId);

        // 访客模式下同步检查关注状态
        checkFollowingStatus(_profileUserId);

        // 访客模式：查看目标用户的统计数据
        fetchStats(_profileUserId);
        loadPublishedArticles(1);
    }
}

/* ================= 核心：关注与统计模块 ================= */

// 1. 独立抽离更新关注按钮UI的逻辑
function updateFollowButtonUI() {
    var btn = document.getElementById('followBtn');
    if (!btn) return;

    if (_isFollowing) {
        btn.innerHTML = '<i class="fas fa-user-check"></i> 已关注';
        btn.className = 'btn btn-primary'; // 确保加上实心样式
    } else {
        btn.innerHTML = '<i class="fas fa-user-plus"></i> 关注';
        btn.className = 'btn btn-ghost'; // 确保加上空心样式
    }
}

// 2. 检查初始关注状态
function checkFollowingStatus(userId) {
    if (!_currentLoginId) return; // 未登录无需校验

    fetch(API_BASE + '/follow/check/' + userId, { credentials: 'same-origin' })
        .then(function(r) { return r.json(); })
        .then(function(res) {
            if (res.code === 200) {
                _isFollowing = res.data;
                updateFollowButtonUI();
            }
        })
        .catch(function(err) { console.error('获取关注状态失败:', err); });
}

// 3. 点击触发关注/取消关注
function toggleFollow() {
    // 拦截 1：未登录
    if (!_currentLoginId) {
        showToast('请先登录', 'error');
        setTimeout(function() {
            window.location.href = API_BASE + '/pages/front/login.html';
        }, 1000);
        return;
    }

    // 拦截 2：不能自己关注自己
    if (String(_currentLoginId) === String(_profileUserId)) {
        showToast('无法关注自己', 'error');
        return;
    }

    var btn = document.getElementById('followBtn');
    if(btn) btn.disabled = true;

    var method = _isFollowing ? 'DELETE' : 'POST';

    fetch(API_BASE + '/follow/' + _profileUserId, {
        method: method,
        credentials: 'same-origin'
    })
        .then(function(r) { return r.json(); })
        .then(function(res) {
            if (res.code === 200) {
                _isFollowing = !_isFollowing; // 翻转状态
                updateFollowButtonUI();       // 刷新按钮
                showToast(_isFollowing ? '关注成功' : '已取消关注', 'success');

                // 重新拉取统计数据，刷新关注数
                fetchStats(_profileUserId);
            } else {
                showToast(res.msg || '操作失败', 'error');
            }
        })
        .catch(function() {
            showToast('网络异常，请稍后再试', 'error');
        })
        .finally(function() {
            if(btn) btn.disabled = false; // 恢复按钮点击
        });
}

// 4. 获取用户统计信息
function fetchStats(userId) {
    var url = API_BASE + '/user/getStats';
    if (userId) {
        url += '?userId=' + userId;
    }
    fetch(url, { credentials: 'same-origin' })
        .then(function (r) { return r.json(); })
        .then(function (res) {
            if (res.code === 200 && res.data) {
                var statFollow  = document.getElementById('statFollow');
                var statFans    = document.getElementById('statFans');
                var statView    = document.getElementById('statView');
                var statLike    = document.getElementById('statLike');
                var statComment = document.getElementById('statComment');

                // followCount ：该用户的关注数，likeCount 包含文章+评论获赞
                if (statFollow)  statFollow.innerText  = formatNumber(res.data.followCount);
                if (statFans)    statFans.innerText    = formatNumber(res.data.fansCount);
                if (statComment) statComment.innerText = formatNumber(res.data.commentCount);
                if (statView)    statView.innerText    = formatNumber(res.data.viewCount);
                if (statLike)    statLike.innerText    = formatNumber(res.data.likeCount);

            }
        })
        .catch(function () {});
}



/* 加载个人资料 —— owner 模式 */
function fetchProfileOwner() {
    fetch(API_BASE + '/user/getProfile', { credentials: 'same-origin' })
        .then(function (r) { return r.json(); })
        .then(function (res) {
            if (res.code === 200) {
                var u   = res.data.user;
                var rId = res.data.roleId;

                document.getElementById('userId').value              = u.id;
                document.getElementById('nicknameDisplay').innerText = u.nickname    || '未设置昵称';
                document.getElementById('usernameDisplay').innerText = '@' + u.username;
                document.getElementById('emailDisplay').innerText    = u.email       || '未绑定';
                document.getElementById('bioDisplay').innerText      = u.bio         || '这个人很懒，暂时没有简介。';
                document.getElementById('nicknameInput').value       = u.nickname    || '';
                document.getElementById('emailInput').value          = u.email       || '';
                document.getElementById('bioInput').value            = u.bio         || '';

                var roleEl = document.getElementById('roleDisplay');
                if (rId == 2) {
                    roleEl.innerText   = '管理员';
                    roleEl.style.color = '#e53e3e';
                } else {
                    roleEl.innerText   = '普通用户';
                    roleEl.style.color = '#4a5568';
                }

                var fmt = function (t) { return t ? String(t).replace('T', ' ').split('.')[0] : '---'; };
                document.getElementById('createTimeDisplay').innerText  = fmt(u.createTime);
                document.getElementById('lastLoginDisplay').innerText   = fmt(u.lastLoginTime);
                document.getElementById('updateTimeDisplay').innerText  = fmt(u.updateTime);

                if (u.avatarUrl) document.getElementById('avatarDisplay').src = u.avatarUrl;

                var badge = document.getElementById('statusBadge');
                badge.innerText = (u.status === 1) ? '正常' : '已封禁';
                badge.className = (u.status === 1) ? 'status-badge status-ok' : 'status-badge status-error';

                // 若是管理员，在 ownerActions 区域追加"进入后台"按钮
                if (rId == 2) {
                    injectAdminButton();
                }
            } else {
                window.location.href = API_BASE + '/pages/front/login.html';
            }
        })
        .catch(function (err) { console.error('加载个人资料出错:', err); });
}

function injectAdminButton() {
    var ownerActions = document.getElementById('ownerActions');
    if (!ownerActions || document.getElementById('adminEntryBtn')) return;
    var btn = document.createElement('a');
    btn.id        = 'adminEntryBtn';
    btn.href      = API_BASE + '/pages/admin/dashboard.html';
    btn.className = 'btn btn-primary';
    btn.innerHTML = '<i class="fas fa-tools"></i> 进入后台';
    btn.style.cssText = 'text-decoration:none;';
    ownerActions.appendChild(btn);
}

/* 加载个人资料 —— visitor 模式 */
function fetchProfileVisitor(userId) {
    fetch(API_BASE + '/user/publicProfile?id=' + userId, { credentials: 'same-origin' })
        .then(function (r) { return r.json(); })
        .then(function (res) {
            if (res.code === 200 && res.data) {
                renderVisitorProfile(res.data);
            } else {
                showToast('用户不存在或已注销', 'error');
            }
        })
        .catch(function () {
            showToast('加载用户信息失败', 'error');
        });
}

function renderVisitorProfile(data) {
    var u   = data.user || data;
    var rId = data.roleId || 1;

    document.getElementById('userId').value              = u.id || _profileUserId;
    document.getElementById('nicknameDisplay').innerText = u.nickname || ('用户 ' + _profileUserId);
    document.getElementById('usernameDisplay').innerText = '@' + (u.username || 'user');
    document.getElementById('emailDisplay').innerText    = u.email || '---';
    document.getElementById('bioDisplay').innerText      = u.bio   || '这个人很懒，暂时没有简介。';

    var roleEl = document.getElementById('roleDisplay');
    if (rId == 2) {
        roleEl.innerText   = '管理员';
        roleEl.style.color = '#e53e3e';
    } else {
        roleEl.innerText   = '普通用户';
        roleEl.style.color = '#4a5568';
    }

    var fmt = function (t) { return t ? String(t).replace('T', ' ').split('.')[0] : '---'; };
    document.getElementById('createTimeDisplay').innerText  = fmt(u.createTime);
    document.getElementById('lastLoginDisplay').innerText   = fmt(u.lastLoginTime);
    document.getElementById('updateTimeDisplay').innerText  = fmt(u.updateTime);

    if (u.avatarUrl) document.getElementById('avatarDisplay').src = u.avatarUrl;

    var badge = document.getElementById('statusBadge');
    badge.innerText = (u.status === 1) ? '正常' : '已封禁';
    badge.className = (u.status === 1) ? 'status-badge status-ok' : 'status-badge status-error';
}

/* 编辑资料（仅 owner） */
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
    var updateData = {
        id:        document.getElementById('userId').value,
        nickname:  document.getElementById('nicknameInput').value,
        email:     document.getElementById('emailInput').value,
        bio:       document.getElementById('bioInput').value,
        avatarUrl: document.getElementById('avatarDisplay').src
    };
    fetch(API_BASE + '/user/updateProfile', {
        method:  'POST',
        credentials: 'same-origin',
        headers: { 'Content-Type': 'application/json' },
        body:    JSON.stringify(updateData)
    })
        .then(function (r) { return r.json(); })
        .then(function (res) {
            if (res.code === 200) {
                showToast('资料修改成功！', 'success');
                setTimeout(function () { location.reload(); }, 1200);
            } else {
                showToast('更新失败：' + res.msg, 'error');
            }
        });
}

function uploadAvatar(input) {
    if (!input.files || !input.files[0]) return;
    var fd = new FormData();
    fd.append('file', input.files[0]);
    fetch(API_BASE + '/user/uploadAvatar', { method: 'POST', credentials: 'same-origin', body: fd })
        .then(function (r) { return r.json(); })
        .then(function (res) {
            if (res.code === 200) {
                document.getElementById('avatarDisplay').src = res.data;
                showToast('预览头像已更新，点击【保存修改】生效', 'info');
            } else {
                showToast('上传失败：' + res.msg, 'error');
            }
        });
}

/* 文章 Tab 切换 */
function switchTab(tab) {
    document.querySelectorAll('.article-tab').forEach(function (b) { b.classList.remove('active'); });
    document.querySelector('.article-tab[data-tab="' + tab + '"]').classList.add('active');
    document.getElementById('tabPublished').style.display = (tab === 'published') ? 'block' : 'none';
    document.getElementById('tabDrafts').style.display    = (tab === 'drafts')    ? 'block' : 'none';
}

/* 已发布文章列表 */
var publishedPage     = 1;
var publishedPageSize = 5;

function loadPublishedArticles(page) {
    publishedPage = page;
    var container = document.getElementById('publishedList');
    container.innerHTML = '<div class="articles-loading"><i class="fas fa-spinner fa-spin"></i> 加载中...</div>';

    var url = _isOwner
        ? ('/article/my/published?page=' + page + '&size=' + publishedPageSize)
        : ('/article/my/published?page=' + page + '&size=' + publishedPageSize + '&userId=' + _profileUserId);

    fetch(API_BASE + url, { credentials: 'same-origin' })
        .then(function (r) { return r.json(); })
        .then(function (res) {
            if (res.code === 200 && res.data) {
                var records = res.data.records || [];
                var total   = res.data.total   || 0;
                document.getElementById('publishedCount').innerText = total;
                renderArticleItems(container, records, false, _isOwner);
                renderPagination('publishedPagination', total, page, publishedPageSize, 'loadPublishedArticles');
            } else {
                renderEmpty(container, '暂无已发布文章');
                document.getElementById('publishedCount').innerText = 0;
            }
        })
        .catch(function () { renderEmpty(container, '加载失败，请刷新重试'); });
}

/* 草稿列表（仅 owner） */
var draftsPage     = 1;
var draftsPageSize = 5;

function loadDraftArticles(page) {
    if (!_isOwner) return;
    draftsPage = page;
    var container = document.getElementById('draftsList');
    container.innerHTML = '<div class="articles-loading"><i class="fas fa-spinner fa-spin"></i> 加载中...</div>';

    fetch(API_BASE +'/article/my/drafts?page=' + page + '&size=' + draftsPageSize, { credentials: 'same-origin' })
        .then(function (r) { return r.json(); })
        .then(function (res) {
            if (res.code === 200 && res.data) {
                var records = res.data.records || [];
                var total   = res.data.total   || 0;
                document.getElementById('draftsCount').innerText = total;
                renderArticleItems(container, records, true, true);
                renderPagination('draftsPagination', total, page, draftsPageSize, 'loadDraftArticles');
            } else {
                renderEmpty(container, '草稿箱是空的');
                document.getElementById('draftsCount').innerText = 0;
            }
        })
        .catch(function () { renderEmpty(container, '加载失败，请刷新重试'); });
}

/* 渲染文章列表 */
function renderArticleItems(container, records, isDraft, isOwnerView) {
    if (!records || records.length === 0) {
        renderEmpty(container, isDraft ? '草稿箱是空的' : '暂无已发布文章');
        return;
    }

    var html = '';
    for (var i = 0; i < records.length; i++) {
        var a = records[i];
        var title   = escapeHtml(a.articleTitle   || '未命名文章');
        var summary = escapeHtml(a.articleSummary || '');
        var tags    = Array.isArray(a.tags) ? a.tags : [];

        var tagsHtml = '';
        for (var j = 0; j < tags.length; j++) {
            tagsHtml += '<span class="article-manage-tag">' + escapeHtml(tags[j]) + '</span>';
        }

        var thumbInner = a.coverImageUrl
            ? '<img src="' + a.coverImageUrl + '" alt="' + title + '" '
            + 'onerror="this.parentElement.innerHTML=\'<i class=&quot;fas fa-file-alt&quot;></i>\'">'
            : '<i class="fas fa-file-alt"></i>';

        var timeLabel = isDraft ? '创建' : '发布';
        var timeVal   = isDraft ? formatArticleTime(a.createTime) : formatArticleTime(a.publishTime);

        var actionHtml = '';
        if (isOwnerView) {
            if (isDraft) {
                actionHtml +=
                    '<button class="article-action-btn btn-publish-draft" '
                    + 'onclick="openPublishDraftModal(' + a.id + ', \'' + title.replace(/'/g, '\\\'') + '\')">'
                    + '<i class="fas fa-paper-plane"></i> 发布</button>';
            }
            actionHtml +=
                '<button class="article-action-btn btn-edit" onclick="openEditArticle(' + a.id + ')">'
                + '<i class="fas fa-edit"></i> 编辑</button>';
            actionHtml +=
                '<button class="article-action-btn btn-delete" '
                + 'onclick="confirmDeleteArticle(' + a.id + ', \'' + title.replace(/'/g, '\\\'') + '\')">'
                + '<i class="fas fa-trash-alt"></i> 删除</button>';
        }

        html += '<div class="article-manage-item">';
        html +=   '<div class="article-manage-thumb">' + thumbInner + '</div>';
        html +=   '<div class="article-manage-info" onclick="goToArticleDetail(' + a.id + ')">';
        html +=     '<div class="article-manage-title">' + title + '</div>';
        if (summary) {
            html += '<div class="article-manage-summary">' + summary + '</div>';
        }
        if (tagsHtml) {
            html += '<div class="article-manage-tags">' + tagsHtml + '</div>';
        }
        html +=     '<div class="article-manage-meta">';
        html +=       '<span><i class="fas fa-clock"></i>&nbsp;' + timeLabel + ':&nbsp;' + timeVal + '</span>';
        html +=       '<span><i class="fas fa-eye"></i>&nbsp;' + formatNumber(a.viewCount) + '</span>';
        html +=       '<span><i class="fas fa-thumbs-up"></i>&nbsp;' + formatNumber(a.likeCount) + '</span>';
        html +=       '<span><i class="fas fa-comment"></i>&nbsp;' + formatNumber(a.commentCount) + '</span>';
        html +=     '</div>';
        html +=   '</div>';
        if (actionHtml) {
            html += '<div class="article-manage-actions">' + actionHtml + '</div>';
        }
        html += '</div>';
    }

    container.innerHTML = html;
}

function renderEmpty(container, msg) {
    container.innerHTML =
        '<div class="articles-empty"><i class="fas fa-file-alt"></i><span>' + msg + '</span></div>';
}

function renderPagination(elId, total, currentPage, pageSize, callbackName) {
    var el = document.getElementById(elId);
    if (!el) return;
    var totalPages = Math.ceil((total || 0) / pageSize);
    if (totalPages <= 1) { el.innerHTML = ''; return; }

    var html = '';
    html += '<button class="page-btn" onclick="' + callbackName + '(' + (currentPage - 1) + ')"'
        + (currentPage <= 1 ? ' disabled' : '') + '>‹</button>';

    var start = Math.max(1, currentPage - 2);
    var end   = Math.min(totalPages, currentPage + 2);
    for (var i = start; i <= end; i++) {
        html += '<button class="page-btn' + (i === currentPage ? ' active' : '') + '"'
            + ' onclick="' + callbackName + '(' + i + ')">' + i + '</button>';
    }

    html += '<button class="page-btn" onclick="' + callbackName + '(' + (currentPage + 1) + ')"'
        + (currentPage >= totalPages ? ' disabled' : '') + '>›</button>';

    el.innerHTML = html;
}

/* 文章操作 */
function goToArticleDetail(id) {
    window.open(API_BASE + '/pages/front/post.html?id=' + id, '_blank');
}

function openEditArticle(id) {
    window.location.href = API_BASE + '/pages/front/editor.html?id=' + id;
}

function closeEditArticle() {
    var modal = document.getElementById('editArticleModal');
    if (modal) modal.classList.remove('active');
}

/* 发布草稿弹窗 */
var _pendingPublishId = null;

function openPublishDraftModal(id, title) {
    _pendingPublishId = id;
    var desc = document.getElementById('publishDraftDesc');
    if (desc) {
        desc.innerHTML =
            '「' + escapeHtml(title) + '」发布后将对<strong>所有人可见</strong>，你仍可随时编辑修改。';
    }
    var confirmBtn = document.getElementById('confirmPublishDraftBtn');
    if (confirmBtn) confirmBtn.onclick = doPublishDraft;
    document.getElementById('publishDraftModal').classList.add('active');
}

function closePublishDraftModal() {
    document.getElementById('publishDraftModal').classList.remove('active');
    _pendingPublishId = null;
}

function doPublishDraft() {
    if (!_pendingPublishId) return;
    var id = _pendingPublishId;

    var confirmBtn = document.getElementById('confirmPublishDraftBtn');
    if (confirmBtn) {
        confirmBtn.disabled = true;
        confirmBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 发布中...';
    }

    fetch(API_BASE +'/article/' + id + '/publish', { method: 'POST', credentials: 'same-origin' })
        .then(function (r) { return r.json(); })
        .then(function (res) {
            closePublishDraftModal();
            if (res.code === 200) {
                showToast('草稿发布成功！', 'success');
                loadPublishedArticles(1);
                loadDraftArticles(1);
            } else {
                showToast(res.msg || '发布失败', 'error');
            }
        })
        .catch(function () {
            closePublishDraftModal();
            showToast('网络异常，请稍后再试', 'error');
        })
        .finally(function () {
            if (confirmBtn) {
                confirmBtn.disabled = false;
                confirmBtn.innerHTML = '<i class="fas fa-paper-plane"></i> 确认发布';
            }
        });
}

/* 删除文章弹窗 */
var _pendingDeleteId = null;
var _deleteMode      = 'article';

function confirmDeleteArticle(id, title) {
    _pendingDeleteId = id;
    _deleteMode      = 'article';

    var body = document.querySelector('#deleteModal .confirm-body');
    if (body) {
        body.innerHTML =
            '<div class="confirm-icon" style="background:#fef2f2;color:#dc2626;">'
            + '<i class="fas fa-trash-alt"></i></div>'
            + '<h4>确认删除此文章？</h4>'
            + '<p>「' + escapeHtml(title) + '」将被<strong>永久删除</strong>，无法找回。</p>';
    }

    var confirmBtn = document.querySelector('#deleteModal .btn-danger');
    if (confirmBtn) {
        confirmBtn.innerHTML = '<i class="fas fa-trash-alt"></i> 确认删除';
        confirmBtn.onclick   = doDeleteArticle;
    }

    document.getElementById('deleteModal').classList.add('active');
}

function doDeleteArticle() {
    if (!_pendingDeleteId) return;
    var id = _pendingDeleteId;

    var confirmBtn = document.querySelector('#deleteModal .btn-danger');
    if (confirmBtn) {
        confirmBtn.disabled = true;
        confirmBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 删除中...';
    }

    fetch(API_BASE + '/article/' + id, { method: 'DELETE', credentials: 'same-origin' })
        .then(function (r) { return r.json(); })
        .then(function (res) {
            closeDeleteConfirm();
            if (res.code === 200) {
                showToast('文章已删除', 'success');
                loadPublishedArticles(publishedPage);
                loadDraftArticles(draftsPage);
            } else {
                showToast(res.msg || '删除失败', 'error');
            }
        })
        .catch(function () {
            closeDeleteConfirm();
            showToast('网络异常，请稍后再试', 'error');
        })
        .finally(function () {
            if (confirmBtn) {
                confirmBtn.disabled = false;
                confirmBtn.innerHTML = '<i class="fas fa-trash-alt"></i> 确认删除';
            }
        });
}

/* 修改密码 Modal */
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
    var oldPwd  = document.getElementById('oldPassword').value.trim();
    var newPwd  = document.getElementById('newPassword').value.trim();
    var confPwd = document.getElementById('confirmPassword').value.trim();

    if (!oldPwd || !newPwd || !confPwd) { showToast('请填写所有密码字段', 'error'); return; }
    if (newPwd !== confPwd)             { showToast('两次输入的新密码不一致', 'error'); return; }
    if (newPwd.length < 6)             { showToast('新密码长度不能少于 6 位', 'error'); return; }

    fetch(API_BASE + '/user/updatePassword', {
        method:  'POST',
        credentials: 'same-origin',
        headers: { 'Content-Type': 'application/json' },
        body:    JSON.stringify({ oldPassword: oldPwd, newPassword: newPwd })
    })
        .then(function (r) { return r.json(); })
        .then(function (res) {
            if (res.code === 200) {
                showToast('密码修改成功，请重新登录', 'success');
                closeChangePassword();
                setTimeout(function () { window.location.href = API_BASE + '/pages/front/login.html'; }, 1500);
            } else {
                showToast(res.msg || '修改失败', 'error');
            }
        })
        .catch(function () { showToast('网络异常，请稍后再试', 'error'); });
}

function checkPwdStrength(val) {
    var bars  = [
        document.getElementById('pBar1'), document.getElementById('pBar2'),
        document.getElementById('pBar3'), document.getElementById('pBar4')
    ];
    var label = document.getElementById('pLabel');
    bars.forEach(function (b) { b.className = 'pwd-bar'; });
    if (!val) { label.innerText = ''; return; }
    var score = 0;
    if (val.length >= 8)          score++;
    if (/[A-Z]/.test(val))        score++;
    if (/[0-9]/.test(val))        score++;
    if (/[^A-Za-z0-9]/.test(val)) score++;
    var levels = ['', 'weak', 'fair', 'good', 'strong'];
    var names  = ['', '弱', '中', '良', '强'];
    for (var i = 0; i < score; i++) bars[i].classList.add(levels[score]);
    label.innerText = names[score] || '';
}

function resetPwdStrength() {
    ['pBar1', 'pBar2', 'pBar3', 'pBar4'].forEach(function (id) {
        document.getElementById(id).className = 'pwd-bar';
    });
    document.getElementById('pLabel').innerText = '';
}

/* 注销账号 Modal */
function openDeleteConfirm() {
    _deleteMode = 'logout';

    var body = document.querySelector('#deleteModal .confirm-body');
    if (body) {
        body.innerHTML =
            '<div class="confirm-icon" style="background:#fef2f2;color:#dc2626;">'
            + '<i class="fas fa-user-times"></i></div>'
            + '<h4>确认注销账号？</h4>'
            + '<p>此操作将<strong>永久删除</strong>你的账号及所有数据，无法恢复。请输入密码确认。</p>'
            + '<div class="modal-field" style="margin-top:16px;text-align:left;">'
            + '<label style="font-size:0.72rem;font-weight:700;color:#dc2626;'
            + 'letter-spacing:0.5px;text-transform:uppercase;display:block;margin-bottom:6px;">'
            + '验证当前密码</label>'
            + '<div class="modal-field-wrap">'
            + '<input type="password" id="deleteConfirmPassword" placeholder="输入密码以确认注销">'
            + '</div></div>';
    }

    var confirmBtn = document.querySelector('#deleteModal .btn-danger');
    if (confirmBtn) {
        confirmBtn.innerHTML = '<i class="fas fa-user-times"></i> 确认注销';
        confirmBtn.onclick   = submitDeleteAccount;
    }

    document.getElementById('deleteModal').classList.add('active');
}

function closeDeleteConfirm() {
    document.getElementById('deleteModal').classList.remove('active');
    _pendingDeleteId = null;
}

function submitDeleteAccount() {
    var inp = document.getElementById('deleteConfirmPassword');
    var pwd = inp ? inp.value.trim() : '';

    if (!pwd) { showToast('请输入密码以确认注销', 'error'); return; }

    var confirmBtn = document.querySelector('#deleteModal .btn-danger');
    if (confirmBtn) {
        confirmBtn.disabled = true;
        confirmBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 注销中...';
    }

    fetch(API_BASE + '/user/deleteAccount', {
        method:      'POST',
        credentials: 'same-origin',
        headers:     { 'Content-Type': 'application/json' },
        body:        JSON.stringify({ password: pwd })
    })
        .then(function (r) { return r.json(); })
        .then(function (res) {
            if (res.code === 200) {
                closeDeleteConfirm();
                showToast('账号已注销，感谢使用', 'info');
                setTimeout(function () {
                    window.location.href = API_BASE + '/pages/front/login.html';
                }, 1500);
            } else {
                showToast(res.msg || '注销失败，请检查密码', 'error');
                if (confirmBtn) {
                    confirmBtn.disabled = false;
                    confirmBtn.innerHTML = '<i class="fas fa-user-times"></i> 确认注销';
                }
            }
        })
        .catch(function () {
            showToast('网络异常，请稍后再试', 'error');
            if (confirmBtn) {
                confirmBtn.disabled = false;
                confirmBtn.innerHTML = '<i class="fas fa-user-times"></i> 确认注销';
            }
        });
}

function submitLogout() { openDeleteConfirm(); }