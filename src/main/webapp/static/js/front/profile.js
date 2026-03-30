/* ==========================================================================
   profile.js — NanoBlog 个人中心交互逻辑
   依赖：jQuery 3.6+
   ========================================================================== */

/* ============================================================
   工具函数
   ============================================================ */

/**
 * 格式化后端返回的时间（支持数组格式 [y,m,d,h,min] 和标准日期字符串）
 */
function formatFullTime(d) {
    if (!d) return "暂无记录";
    if (Array.isArray(d)) {
        var y   = d[0];
        var mo  = ("0" + d[1]).slice(-2);
        var day = ("0" + d[2]).slice(-2);
        var h   = ("0" + (d[3] || 0)).slice(-2);
        var min = ("0" + (d[4] || 0)).slice(-2);
        return y + "-" + mo + "-" + day + " " + h + ":" + min;
    }
    var dt = new Date(d);
    if (isNaN(dt.getTime())) return d;
    return dt.getFullYear() + "-" + (dt.getMonth() + 1) + "-" + dt.getDate() + " " +
        ("0" + dt.getHours()).slice(-2) + ":" + ("0" + dt.getMinutes()).slice(-2);
}

/**
 * 显示右上角 Toast 通知
 * @param {string} msg   - 消息文本
 * @param {string} type  - "success" | "error"
 */
function showToast(msg, type) {
    type = type || "success";
    var t = document.getElementById("toast");
    t.textContent = (type === "success" ? "✅ " : "❌ ") + msg;
    t.className = "toast " + type + " show";
    setTimeout(function () { t.className = "toast " + type; }, 3200);
}

/* ============================================================
   二次确认弹窗
   ============================================================ */
var _modalCallback = null;

/**
 * 弹出二次确认弹窗
 * @param {string}   title   - 弹窗标题
 * @param {string}   desc    - 描述文字
 * @param {boolean}  danger  - 是否为危险操作（确认按钮变红）
 * @param {Function} cb      - 用户点击确认后的回调
 */
function showModal(title, desc, danger, cb) {
    document.getElementById("modalTitle").textContent = title;
    document.getElementById("modalDesc").textContent  = desc;
    var confirmBtn = document.getElementById("modalConfirm");
    confirmBtn.className = "btn-confirm" + (danger ? " danger-confirm" : "");
    _modalCallback = cb;
    document.getElementById("modalOverlay").classList.add("show");
}

document.getElementById("modalCancel").onclick = function () {
    document.getElementById("modalOverlay").classList.remove("show");
};

document.getElementById("modalConfirm").onclick = function () {
    document.getElementById("modalOverlay").classList.remove("show");
    if (_modalCallback) _modalCallback();
};

/* ============================================================
   侧边栏面板切换
   ============================================================ */
document.querySelectorAll(".nav-item[data-panel]").forEach(function (item) {
    item.addEventListener("click", function () {
        var target = this.dataset.panel;

        document.querySelectorAll(".nav-item").forEach(function (n) {
            n.classList.remove("active");
        });
        this.classList.add("active");

        document.querySelectorAll(".panel").forEach(function (p) {
            p.classList.remove("active");
        });
        document.getElementById("panel-" + target).classList.add("active");
    });
});

/* ============================================================
   输入框聚焦动效（复用 auth.css 的 .field-wrap 体系）
   ============================================================ */
document.querySelectorAll(".field-wrap").forEach(function (wrap) {
    var input = wrap.querySelector(".field-input");
    if (!input) return;
    input.addEventListener("focus", function () { wrap.classList.add("focused"); });
    input.addEventListener("blur",  function () { wrap.classList.remove("focused"); });
});

/* ============================================================
   密码可见性切换
   ============================================================ */
document.querySelectorAll(".field-eye").forEach(function (btn) {
    btn.addEventListener("click", function () {
        var inp = document.getElementById(this.dataset.target);
        if (!inp) return;
        inp.type = inp.type === "password" ? "text" : "password";
        this.classList.toggle("active");
    });
});

/* ============================================================
   密码强度检测
   ============================================================ */
document.getElementById("newPassword").addEventListener("input", function () {
    var val = this.value;
    var strength = 0;
    if (val.length >= 8)            strength++;
    if (/[A-Z]/.test(val))          strength++;
    if (/[0-9]/.test(val))          strength++;
    if (/[^A-Za-z0-9]/.test(val))   strength++;

    var levels = ["weak", "fair", "good", "strong"];
    var labels = ["弱", "中", "强", "强"];

    ["bar1", "bar2", "bar3", "bar4"].forEach(function (id, i) {
        var el = document.getElementById(id);
        el.className = "strength-bar" + (i < strength ? " " + levels[strength - 1] : "");
    });

    document.getElementById("strengthLabel").textContent = val ? labels[Math.max(0, strength - 1)] : "";
});

/* ============================================================
   页面初始化：鉴权 + 加载个人资料
   ============================================================ */
$(document).ready(function () {

    // 未登录拦截
    var loginUser = localStorage.getItem("loginUsername");
    if (!loginUser) {
        alert("检测到您尚未登录，请先登录！");
        window.location.href = "/NanoBlog_war/pages/front/login.html";
        return;
    }

    // 从 Session 加载个人资料
    $.get("/NanoBlog_war/user/getProfile", { username: loginUser }, function (res) {
        if (res.code === 200) {
            var u = res.data;

            // 隐藏字段
            $("#userId").val(u.id);
            $("#avatarUrl").val(u.avatarUrl || "");

            // 表单
            $("#username").val(u.username);
            $("#nickname").val(u.nickname);
            $("#email").val(u.email);
            $("#bio").val(u.bio || "");

            // Hero 横幅
            $("#heroName").text(u.username);
            $("#heroNickname").text(u.nickname ? "@" + u.nickname : "@" + u.username);
            document.getElementById("topbarUsername").textContent = u.nickname || u.username;

            if (u.avatarUrl) {
                $("#heroAvatarImg, #topbarAvatar").attr("src", u.avatarUrl);
            }

            // 账号状态
            var isNormal = u.status === 1;
            $("#heroStatus").text(isNormal ? "账号正常" : "账号已禁用");
            $("#userStatus").text(isNormal ? "正常" : "禁用")
                .css("color", isNormal ? "#28a745" : "#dc3545");

            // 时间字段
            $("#createTime").text(formatFullTime(u.createTime));
            $("#updateTime").text(formatFullTime(u.updateTime));
            $("#lastLoginTime").text(formatFullTime(u.lastLoginTime));

        } else {
            alert("Session 已失效，请重新登录");
            window.location.href = "/NanoBlog_war/pages/front/login.html";
        }
    });

    // TODO: 文章数量——等文章模块接口就绪后取消注释
    // $.get("/NanoBlog_war/article/countByUser", { userId: $("#userId").val() }, function (res) {
    //     if (res.code === 200) $("#heroArticles").text(res.data);
    // });

    // TODO: 总浏览量、总获赞、关注数——等对应接口就绪后接入
    // $.get("/NanoBlog_war/stat/userSummary", { userId: $("#userId").val() }, function (res) {
    //     if (res.code === 200) {
    //         // res.data: { views, likes, follows }
    //     }
    // });

    // TODO: 文章列表——等文章模块接口就绪后取消注释
    // $.get("/NanoBlog_war/article/listByUser", { userId: $("#userId").val() }, function (res) {
    //     if (res.code === 200) renderArticleList(res.data);
    // });
});

/* ============================================================
   头像上传
   ============================================================ */
document.getElementById("heroAvatarRegion").onclick = function () {
    document.getElementById("fileInput").click();
};

document.getElementById("fileInput").onchange = function () {
    var file = this.files[0];
    if (!file) return;

    var fd = new FormData();
    fd.append("file", file);

    $.ajax({
        url: "/NanoBlog_war/user/uploadAvatar",
        type: "POST",
        data: fd,
        processData: false,
        contentType: false,
        success: function (res) {
            if (res.code === 200) {
                $("#heroAvatarImg, #topbarAvatar").attr("src", res.data);
                $("#avatarUrl").val(res.data);
                showToast("头像上传成功，记得点击保存修改！");
            } else {
                showToast("头像上传失败", "error");
            }
        }
    });
};

/* ============================================================
   保存个人资料
   ============================================================ */
document.getElementById("saveBtn").onclick = function () {
    var data = {
        id:        $("#userId").val(),
        nickname:  $("#nickname").val(),
        email:     $("#email").val(),
        avatarUrl: $("#avatarUrl").val(),
        bio:       $("#bio").val()
    };

    $.ajax({
        url: "/NanoBlog_war/user/updateProfile",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify(data),
        success: function (res) {
            if (res.code === 200) {
                showToast("资料已成功更新！");
                var nn = $("#nickname").val();
                if (nn) {
                    $("#heroNickname").text("@" + nn);
                    document.getElementById("topbarUsername").textContent = nn;
                }
                $("#updateTime").text(formatFullTime(new Date()));
            } else {
                showToast("保存失败：" + res.msg, "error");
            }
        }
    });
};

/* ============================================================
   修改密码
   ============================================================ */
document.getElementById("changePwdBtn").onclick = function () {
    var oldPwd     = document.getElementById("oldPassword").value;
    var newPwd     = document.getElementById("newPassword").value;
    var confirmPwd = document.getElementById("confirmPassword").value;

    if (!oldPwd || !newPwd || !confirmPwd) {
        showToast("请填写所有密码字段", "error"); return;
    }
    if (newPwd !== confirmPwd) {
        showToast("两次输入的新密码不一致", "error"); return;
    }
    if (newPwd.length < 6) {
        showToast("新密码长度至少 6 位", "error"); return;
    }

    // TODO: 等后端 /user/changePassword 接口就绪后取消注释
    // $.ajax({
    //     url: "/NanoBlog_war/user/changePassword",
    //     type: "POST",
    //     contentType: "application/json",
    //     data: JSON.stringify({
    //         id: $("#userId").val(),
    //         oldPassword: oldPwd,
    //         newPassword: newPwd
    //     }),
    //     success: function (res) {
    //         if (res.code === 200) {
    //             showToast("密码修改成功，请重新登录");
    //             setTimeout(doLogout, 1500);
    //         } else {
    //             showToast(res.msg || "密码修改失败", "error");
    //         }
    //     }
    // });

    showToast("密码修改接口待对接", "error");
};

/* ============================================================
   退出登录
   ============================================================ */
function doLogout() {
    $.post("/NanoBlog_war/user/logout", function () {}).always(function () {
        localStorage.removeItem("loginUsername");
        window.location.href = "/NanoBlog_war/pages/front/login.html";
    });
}

document.getElementById("topbarLogout").onclick = function () {
    showModal("退出登录", "确认退出当前账号并返回登录页面吗？", false, doLogout);
};

document.getElementById("logoutBtn2").onclick = function () {
    showModal("退出登录", "确认退出当前账号并返回登录页面吗？", false, doLogout);
};

/* ============================================================
   注销账号
   ============================================================ */
document.getElementById("deactivateBtn").onclick = function () {
    showModal(
        "⚠️ 注销账号",
        "此操作将永久删除您的账号及所有数据，且不可撤销。您确定要继续吗？",
        true,
        function () {
            // TODO: 等后端 /user/deactivate 接口就绪后取消注释
            // $.post("/NanoBlog_war/user/deactivate",
            //     { id: $("#userId").val() },
            //     function (res) {
            //         if (res.code === 200) doLogout();
            //         else showToast(res.msg || "注销失败", "error");
            //     }
            // );
            showToast("账号注销接口待对接", "error");
        }
    );
};

/* ============================================================
   文章列表渲染（待后端对接后调用）
   ============================================================ */
// function renderArticleList(articles) {
//     var container = document.getElementById("articleListContainer");
//     if (!articles || articles.length === 0) {
//         container.innerHTML = '<div class="stub-state"><div class="stub-icon">📝</div>' +
//             '<div class="stub-title">暂无文章</div>' +
//             '<div class="stub-desc">快去写第一篇文章吧！</div></div>';
//         return;
//     }
//     container.innerHTML = articles.map(function (a) {
//         return '<div class="article-item">' +
//             '<div class="article-main">' +
//             '<div class="article-title">' + a.title + '</div>' +
//             '<div class="article-meta">' +
//             '<span class="article-tag">' + (a.category || "未分类") + '</span>' +
//             '<span class="article-date">' + formatFullTime(a.createTime) + '</span>' +
//             '<div class="article-stats">' +
//             '<span class="article-stat-item">👁 ' + (a.views || 0) + '</span>' +
//             '<span class="article-stat-item">👍 ' + (a.likes || 0) + '</span>' +
//             '<span class="article-stat-item">💬 ' + (a.comments || 0) + '</span>' +
//             '</div></div></div>' +
//             '<div class="article-actions">' +
//             '<button class="btn-sm btn-sm-edit" onclick="editArticle(' + a.id + ')">编辑</button>' +
//             '<button class="btn-sm btn-sm-del" onclick="deleteArticle(' + a.id + ')">删除</button>' +
//             '</div></div>';
//     }).join("");
// }
```

---

**文件存放路径建议：**
```
