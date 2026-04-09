(function (window, document) {
    'use strict';

    function resolveApiBase() {
        if (window.NanoBlog && typeof window.NanoBlog.apiBase === 'string') {
            return window.NanoBlog.apiBase;
        }
        const path = window.location.pathname || '';
        const idx = path.indexOf('/pages/');
        return idx > 0 ? path.substring(0, idx) : '';
    }

    const API_BASE = resolveApiBase();

    //常量和运行状态
    const DEFAULT_AVATAR = API_BASE + '/static/images/avatar-default.png';
    const MAX_LEN = 500;

    const state = {
        articleId: null,  //由init注入
        currentUserId: null,  //null表示未登录
        currentAvatar: DEFAULT_AVATAR,
        openReplyId: null,  //当前展开的根评论 id
    };

    //工具函数
    function toast(msg, icon) {
        if (window.layui && layui.layer) {
            layui.layer.msg(msg, {icon: icon || 2, time: 2000});
        }
    }

    function escHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    //统一时间展示格式: yyyy-MM-dd HH:mm
    //兼容LocalDateTime数组和字符串
    function formatTime(val) {
        if (!val) return '';
        //数组格式[year, month, day, hour, minute, second]
        if (Array.isArray(val)) {
            const [y, mo, d, h = 0, mi = 0] = val;
            return `${y}-${String(mo).padStart(2, '0')}-${String(d).padStart(2, '0')} `
                + `${String(h).padStart(2, '0')}:${String(mi).padStart(2, '0')}`;
        }
        const date = new Date(val);
        //非标准日期字符串尽量按后端原值截断展示
        if (isNaN(date)) return String(val).slice(0, 16).replace('T', ' ');
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} `
            + `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    }

    //获取当前登录用户
    function fetchCurrentUser() {
        return fetch(API_BASE + '/user/getProfile', {credentials: 'same-origin'})
            .then(r => r.json())
            .then(res => {
                if (res.code === 200 && res.data) {
                    state.currentUserId = res.data.user.id;
                    state.currentAvatar = res.data.user.avatarUrl || DEFAULT_AVATAR;
                }
            })
            .catch(() => {
            });
    }

    //渲染评论骨架和空态
    function renderSkeleton() {
        const el = document.getElementById('commentList');
        if (!el) return;
        el.innerHTML = `
            <div class="comment-skeleton">
                ${[1, 2].map(() => `
                <div class="skeleton-item">
                    <div style="display:flex;gap:12px;margin-bottom:12px;">
                        <div class="skeleton-line" style="width:38px;height:38px;border-radius:50%;flex-shrink:0;"></div>
                        <div style="flex:1;display:flex;flex-direction:column;gap:8px;padding-top:4px;">
                            <div class="skeleton-line" style="width:25%;"></div>
                            <div class="skeleton-line" style="width:15%;height:10px;"></div>
                        </div>
                    </div>
                    <div style="display:flex;flex-direction:column;gap:8px;padding-left:50px;">
                        <div class="skeleton-line" style="width:90%;"></div>
                        <div class="skeleton-line" style="width:65%;"></div>
                    </div>
                </div>`).join('')}
            </div>
        `;
    }

    function renderEmpty() {
        const el = document.getElementById('commentList');
        if (!el) return;
        el.innerHTML = `
            <div class="comment-empty">
                <div class="comment-empty-icon">💬</div>
                <p>暂无评论，来发表第一条吧！</p>
            </div>
        `;
    }

    //渲染根评论输入框
    function renderRootEditor() {
        const wrap = document.getElementById('commentEditorWrap');
        if (!wrap) return;

        if (!state.currentUserId) {
            wrap.innerHTML = `
                <div class="comment-login-tip">
                    请先 <a href="${API_BASE}/pages/front/login.html">登录</a> 后再发表评论
                </div>
            `;
            return;
        }

        wrap.innerHTML = `
            <div class="comment-editor-card">
                <div class="comment-editor-top">
                    <img class="comment-user-avatar"
                         src="${escHtml(state.currentAvatar)}"
                         alt="我的头像"
                         onerror="this.src='${DEFAULT_AVATAR}'">
                    <div class="comment-textarea-wrap">
                        <textarea class="comment-textarea"
                                  id="rootCommentInput"
                                  maxlength="${MAX_LEN}"
                                  placeholder="写下你的评论..."
                                  oninput="CommentModule.onRootInput(this)"></textarea>
                    </div>
                </div>
                <div class="comment-editor-footer">
                    <span class="comment-char-count" id="rootCharCount">0 / ${MAX_LEN}</span>
                    <button class="btn-comment-submit"
                            id="rootSubmitBtn"
                            onclick="CommentModule.submitRoot()">发表评论</button>
                </div>
            </div>
        `;
    }

    //渲染树形评论
    function renderCommentList(comments) {
        const el = document.getElementById('commentList');
        if (!el) return;

        if (!comments || comments.length === 0) {
            renderEmpty();
            updateCount(0);
            return;
        }

        // 总数=根评论+所有直接回复
        const total = comments.reduce((acc, c) => acc + 1 + (c.replies ? c.replies.length : 0), 0);
        updateCount(total);

        el.innerHTML = comments.map(comment => renderRootComment(comment)).join('');
    }

    function renderRootComment(comment) {
        const canDelete = state.currentUserId && String(comment.userId) === String(state.currentUserId);
        const repliesHtml = comment.replies && comment.replies.length > 0
            ? `<div class="reply-list">${comment.replies.map(r => renderReplyItem(r)).join('')}</div>`
            : '';

        return `
            <div class="comment-item" id="comment-${comment.id}" data-id="${comment.id}">
                <div class="comment-item-header">
                    <img class="comment-avatar"
                         src="${escHtml(comment.avatarUrl || DEFAULT_AVATAR)}"
                         alt="${escHtml(comment.nickname || '用户')}"
                         onerror="this.src='${DEFAULT_AVATAR}'">
                    <div class="comment-meta">
                        <div class="comment-nickname">${escHtml(comment.nickname || '匿名用户')}</div>
                        <div class="comment-time">${formatTime(comment.createTime)}</div>
                    </div>
                    <div class="comment-actions">
                        <button class="btn-comment-action btn-like-comment"
                            id="likeBtn-${comment.id}"
                            onclick="CommentModule.toggleCommentLike(${comment.id}, this)">
                            <i class="fas fa-heart"></i>
                            <span>${comment.likeCount || 0}</span>
                        </button>
                        ${state.currentUserId ? `
                        <button class="btn-comment-action btn-reply"
                                id="replyBtn-${comment.id}"
                                onclick="CommentModule.toggleReplyEditor(${comment.id})">
                            回复
                        </button>` : ''}
                        ${canDelete ? `
                        <button class="btn-comment-action btn-delete-comment"
                                onclick="CommentModule.deleteComment(${comment.id}, false)">
                            删除
                        </button>` : ''}
                    </div>
                </div>

                <div class="comment-body">${escHtml(comment.commentContent)}</div>

                <div class="reply-editor-wrap" id="replyEditor-${comment.id}">
                    <div class="reply-editor-inner">
                        <textarea class="reply-textarea"
                                  id="replyInput-${comment.id}"
                                  maxlength="${MAX_LEN}"
                                  placeholder="回复 ${escHtml(comment.nickname || '用户')}..."></textarea>
                        <div class="reply-actions">
                            <button class="btn-reply-submit"
                                    onclick="CommentModule.submitReply(${comment.id})">回复</button>
                            <button class="btn-reply-cancel"
                                    onclick="CommentModule.toggleReplyEditor(${comment.id})">取消</button>
                        </div>
                    </div>
                </div>

                ${repliesHtml}
            </div>
        `;
    }

    function renderReplyItem(reply) {
        const canDelete = state.currentUserId &&
            String(reply.userId) === String(state.currentUserId);
        //回复前缀
        const prefix = reply.replyToNickname
            ? `<span style="color:var(--brand-blue);font-weight:600;">回复 @${escHtml(reply.replyToNickname)}：</span>`
            : '';

        return `
        <div class="reply-item" id="comment-${reply.id}">
            <div class="reply-item-header">
                <img class="reply-avatar"
                     src="${escHtml(reply.avatarUrl || DEFAULT_AVATAR)}"
                     alt="${escHtml(reply.nickname || '用户')}"
                     onerror="this.src='${DEFAULT_AVATAR}'">
                <div class="reply-meta">
                    <div class="reply-nickname">${escHtml(reply.nickname || '匿名用户')}</div>
                    <div class="reply-time">${formatTime(reply.createTime)}</div>
                </div>
                <div class="comment-actions">
                    <button class="btn-comment-action btn-like-comment"
                        id="likeBtn-${reply.id}"
                        onclick="CommentModule.toggleCommentLike(${reply.id}, this)">
                        <i class="fas fa-heart"></i>
                        <span>${reply.likeCount || 0}</span>
                    </button>
                    ${state.currentUserId ? `
                    <button class="btn-comment-action btn-reply"
                            data-parent-id="${reply.parentId}"
                            data-reply-id="${reply.id}"
                            data-reply-nickname="${escHtml(reply.nickname || '')}"
                            data-reply-user-id="${reply.userId}"
                            onclick="CommentModule.handleSubReplyClick(this)">
                        回复
                    </button>` : ''}
                    ${canDelete ? `
                    <button class="btn-comment-action btn-delete-comment"
                            onclick="CommentModule.deleteComment(${reply.id}, true)">
                        删除
                    </button>` : ''}
                </div>
            </div>
            <div class="reply-body">${prefix}${escHtml(reply.commentContent)}</div>
        </div>
    `;
    }

    // 更新评论总数
    function updateCount(n) {
        const badge = document.getElementById('commentTotalBadge');
        if (badge) badge.textContent = n;
    }

    // 根评论字数统计
    window.CommentModule = window.CommentModule || {};

    CommentModule.onRootInput = function (textarea) {
        const len = textarea.value.length;
        const el = document.getElementById('rootCharCount');
        if (!el) return;
        el.textContent = `${len} / ${MAX_LEN}`;
        el.classList.toggle('over-limit', len >= MAX_LEN);
    };

    // 加载评论列表
    CommentModule.load = function () {
        if (!state.articleId) return;
        renderSkeleton();

        fetch(`${API_BASE}/comment/list/${state.articleId}`, {credentials: 'same-origin'})
            .then(r => r.json())
            .then(res => {
                if (res.code === 200) {
                    renderCommentList(res.data || []);
                    if (state.currentUserId && res.data && res.data.length > 0) {
                        initAllCommentLikeStatus(res.data);
                    }
                } else {
                    toast('评论加载失败：' + (res.msg || ''));
                    renderEmpty();
                }
            })
            .catch(() => {
                toast('网络异常，评论加载失败');
                renderEmpty();
            });
    };

    // 发表根评论
    CommentModule.submitRoot = function () {
        if (!state.currentUserId) {
            toast('请先登录后再评论');
            return;
        }

        const input = document.getElementById('rootCommentInput');
        const content = input ? input.value.trim() : '';

        if (!content) {
            toast('评论内容不能为空');
            return;
        }
        if (content.length > MAX_LEN) {
            toast(`评论不能超过 ${MAX_LEN} 字`);
            return;
        }

        const btn = document.getElementById('rootSubmitBtn');
        if (btn) {
            btn.disabled = true;
            btn.textContent = '发送中...';
        }

        fetch(API_BASE + '/comment/add', {
            method: 'POST',
            credentials: 'same-origin',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                articleId: state.articleId,
                commentContent: content,
                parentId: 0
            })
        })
            .then(r => r.json())
            .then(res => {
                if (res.code === 200) {
                    if (input) input.value = '';
                    const countEl = document.getElementById('rootCharCount');
                    if (countEl) countEl.textContent = `0 / ${MAX_LEN}`;
                    toast('评论发表成功', 1);
                    CommentModule.load();
                } else {
                    toast(res.msg || '发表失败');
                }
            })
            .catch(() => toast('网络异常，请稍后再试'))
            .finally(() => {
                if (btn) {
                    btn.disabled = false;
                    btn.textContent = '发表评论';
                }
            });
    };

    // 切换回复框
    CommentModule.toggleReplyEditor = function (parentId) {
        // 同一时间只保留一个展开的回复框
        if (state.openReplyId && state.openReplyId !== parentId) {
            const old = document.getElementById(`replyEditor-${state.openReplyId}`);
            if (old) old.classList.remove('open');
            const oldBtn = document.getElementById(`replyBtn-${state.openReplyId}`);
            if (oldBtn) oldBtn.classList.remove('active');
        }

        const wrap = document.getElementById(`replyEditor-${parentId}`);
        const btn = document.getElementById(`replyBtn-${parentId}`);
        if (!wrap) return;

        const isOpen = wrap.classList.contains('open');
        wrap.classList.toggle('open', !isOpen);
        if (btn) btn.classList.toggle('active', !isOpen);
        state.openReplyId = isOpen ? null : parentId;

        if (!isOpen) {
            // 使用微延时等待展开动画/样式生效后再聚焦
            const ta = document.getElementById(`replyInput-${parentId}`);
            if (ta) setTimeout(() => ta.focus(), 50);
        }
    };

    // 发表回复
    CommentModule.submitReply = function (parentId) {
        if (!state.currentUserId) {
            toast('请先登录后再回复');
            return;
        }
        const wrap = document.getElementById(`replyEditor-${parentId}`);
        const input = document.getElementById(`replyInput-${parentId}`);
        const content = input ? input.value.trim() : '';

        if (!content) {
            toast('回复内容不能为空');
            return;
        }
        if (content.length > MAX_LEN) {
            toast(`回复不能超过 ${MAX_LEN} 字`);
            return;
        }
        //被回复人id
        const replyToUserId = wrap && wrap.dataset.replyToUserId
            ? Number(wrap.dataset.replyToUserId)
            : null;

        fetch(API_BASE + '/comment/add', {
            method: 'POST',
            credentials: 'same-origin',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                articleId: state.articleId,
                commentContent: content,
                parentId: parentId,
                replyToUserId: replyToUserId,
            })
        })
            .then(r => r.json())
            .then(res => {
                if (res.code === 200) {
                    toast('回复成功', 1);
                    CommentModule.toggleReplyEditor(parentId);
                    if (input) input.value = '';
                    if (wrap) {
                        delete wrap.dataset.replyToId;
                        delete wrap.dataset.replyToUserId;
                    }
                    CommentModule.load();
                } else {
                    toast(res.msg || '回复失败');
                }
            })
            .catch(() => toast('网络异常，请稍后再试'));
    };

    // 删除评论/回复（isReply 仅用于提示文案）
    CommentModule.deleteComment = function (id, isReply) {
        const label = isReply ? '回复' : '评论';

        if (window.layui && layui.layer) {
            layui.layer.confirm(
                `确定要删除这条${label}吗？`,
                {title: '删除确认', btn: ['确定', '取消'], icon: 3},
                function (index) {
                    layui.layer.close(index);
                    doDelete(id, label);
                }
            );
        } else {
            if (confirm(`确定要删除这条${label}吗？`)) doDelete(id, label);
        }
    };

    function doDelete(id, label) {
        fetch(`${API_BASE}/comment/${id}`, {
            method: 'DELETE',
            credentials: 'same-origin'
        })
            .then(r => r.json())
            .then(res => {
                if (res.code === 200) {
                    toast(`${label}已删除`, 1);
                    CommentModule.load();
                } else {
                    toast(res.msg || `删除失败`);
                }
            })
            .catch(() => toast('网络异常，请稍后再试'));
    }

    //处理子评论的回复（只依赖replyToUserId）
    CommentModule.toggleSubReply = function (parentId, replyId, replyNickname, replyUserId) {
        //确保父评论的回复框打开
        const wrap = document.getElementById(`replyEditor-${parentId}`);
        const ta = document.getElementById(`replyInput-${parentId}`);
        if (!wrap || !ta) return;

        //如果已经在回复同一个人，则关闭
        if (wrap.dataset.replyToId === String(replyId) && wrap.classList.contains('open')) {
            CommentModule.toggleReplyEditor(parentId);
            delete wrap.dataset.replyToId;
            delete wrap.dataset.replyToUserId;
            ta.placeholder = `回复评论...`;
            return;
        }

        // 记录被回复人信息到 DOM dataset
        wrap.dataset.replyToId = replyId;
        wrap.dataset.replyToUserId = replyUserId;
        ta.placeholder = `回复 @${replyNickname}...`;

        //打开回复框
        if (!wrap.classList.contains('open')) {
            CommentModule.toggleReplyEditor(parentId);
        }
        setTimeout(() => ta.focus(), 50);
    };

    CommentModule.handleSubReplyClick = function (btn) {
        const parentId = btn.dataset.parentId;
        const replyId = btn.dataset.replyId;
        const replyNickname = btn.dataset.replyNickname;
        const replyUserId = btn.dataset.replyUserId;
        CommentModule.toggleSubReply(parentId, replyId, replyNickname, replyUserId);
    };

    //批量初始化所有评论（含回复）的点赞状态
    function initAllCommentLikeStatus(comments) {
        comments.forEach(function(comment) {
            initCommentLikeStatus(comment.id);
            if (comment.replies && comment.replies.length > 0) {
                comment.replies.forEach(function(reply) {
                    initCommentLikeStatus(reply.id);
                });
            }
        });
    }

    function initCommentLikeStatus(commentId) {
        fetch(API_BASE.replace('/article', '') + '/comment/like/' + commentId, {
            credentials: 'same-origin'
        })
            .then(function(r) { return r.json(); })
            .then(function(res) {
                if (res.code === 200 && res.data === true) {
                    var btn = document.getElementById('likeBtn-' + commentId);
                    if (btn) btn.classList.add('liked');
                }
            })
            .catch(function() {});
    }

    CommentModule.toggleCommentLike = function(commentId, btn) {
        if (!state.currentUserId) {
            toast('请先登录后再点赞');
            return;
        }
        var isLiked = btn.classList.contains('liked');
        var method  = isLiked ? 'DELETE' : 'POST';

        fetch(API_BASE.replace('/article', '') + '/comment/like/' + commentId, {
            method: method,
            credentials: 'same-origin'
        })
            .then(function(r) { return r.json(); })
            .then(function(res) {
                if (res.code === 200) {
                    var countSpan = btn.querySelector('span');
                    var current   = parseInt(countSpan ? countSpan.textContent : 0) || 0;
                    if (isLiked) {
                        btn.classList.remove('liked');
                        if (countSpan) countSpan.textContent = Math.max(0, current - 1);
                    } else {
                        btn.classList.add('liked');
                        if (countSpan) countSpan.textContent = current + 1;
                    }
                } else {
                    toast(res.msg || '操作失败');
                }
            })
            .catch(function() { toast('网络异常'); });
    };

    // 入口: 在 post.js 初始化后调用
    CommentModule.init = function (articleId) {
        state.articleId = articleId;

        // 先拿登录态，避免编辑器和按钮渲染错位
        fetchCurrentUser().then(() => {
            renderRootEditor();
            CommentModule.load();
        });
    };

    window.CommentModule = CommentModule;

})(window, document);