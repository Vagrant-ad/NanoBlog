// 粒子背景
(function() {
    const canvas = document.getElementById('particleCanvas');
    const ctx = canvas.getContext('2d');
    const particles = [];
    function resize() { canvas.width = window.innerWidth; canvas.height = window.innerHeight; }
    resize();
    window.addEventListener('resize', resize);
    for (let i = 0; i < 40; i++) {
        particles.push({
            x: Math.random() * window.innerWidth,
            y: Math.random() * window.innerHeight,
            r: Math.random() * 3 + 1,
            dx: (Math.random() - 0.5) * 0.4,
            dy: (Math.random() - 0.5) * 0.4,
            alpha: Math.random() * 0.4 + 0.1
        });
    }
    function draw() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        particles.forEach(p => {
            ctx.beginPath();
            ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
            ctx.fillStyle = `rgba(163, 216, 224, ${p.alpha})`;
            ctx.fill();
            p.x += p.dx; p.y += p.dy;
            if (p.x < 0 || p.x > canvas.width) p.dx *= -1;
            if (p.y < 0 || p.y > canvas.height) p.dy *= -1;
        });
        requestAnimationFrame(draw);
    }
    draw();
})();

// 密码显示切换
document.getElementById('togglePwd').addEventListener('click', function() {
    const pwd = document.getElementById('passwordHash');
    pwd.type = pwd.type === 'password' ? 'text' : 'password';
    this.classList.toggle('active');
});

// 密码强度检测
document.getElementById('passwordHash').addEventListener('input', function() {
    const val = this.value;
    const bars = ['s1','s2','s3','s4'].map(id => document.getElementById(id));
    const label = document.getElementById('sLabel');
    let score = 0;
    if (val.length >= 6) score++;
    if (val.length >= 10) score++;
    if (/[A-Z]/.test(val) && /[0-9]/.test(val)) score++;
    if (/[^A-Za-z0-9]/.test(val)) score++;
    const cls = ['', 'weak', 'fair', 'good', 'strong'];
    const lbs = ['', '弱', '一般', '较强', '强'];
    bars.forEach((b, i) => { b.className = 'strength-bar' + (i < score ? ' ' + cls[score] : ''); });
    label.textContent = val ? (lbs[score] || '强') : '';
});

// 字段聚焦动效
document.querySelectorAll('.field-input').forEach(input => {
    input.addEventListener('focus', () => input.closest('.field-wrap').classList.add('focused'));
    input.addEventListener('blur', () => {
        input.closest('.field-wrap').classList.remove('focused');
        input.value
            ? input.closest('.field-wrap').classList.add('has-value')
            : input.closest('.field-wrap').classList.remove('has-value');
    });
});

// ===== 以下为原有核心逻辑，不做任何修改 =====
$("#regBtn").click(function() {
    var username = $("#username").val();
    var password = $("#passwordHash").val();
    var nickname = $("#nickname").val();
    var email = $("#email").val();
    var emailCode = $("#emailCode").val();
    var roleId = $("#roleId").val();

    if (!username || !password || !nickname || !email) {
        layer.msg("请完整填写注册信息！", {icon: 7});
        return;
    }
    if (!emailCode) {
        layer.msg("请输入邮箱验证码！", {icon: 7});
        return;
    }

    $.ajax({
        url: "/user/doRegister",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({
            username: username,
            password: password,
            nickname: nickname,
            email: email,
            roleId: roleId
        }),
        success: function(res) {
            if (res.code === 200) {
                layer.msg("注册成功！即将跳转登录", {icon: 1, time: 1500}, function(){
                    window.location.href = "login.html";
                });
            } else {
                layer.msg(res.msg, {icon: 2});
            }
        },
        error: function() {
            layer.msg("网络请求失败，请稍后再试", {icon: 2});
            // 4. 加一行打印日志，如果还报错，我们可以直接看控制台的原因
            console.error("请求报错状态码:", xhr.status, "报错详情:", xhr.responseText);
            layer.msg("网络请求失败，请稍后再试", {icon: 2});
            $(this).text("注 册").removeClass("loading");
        }
    });
});