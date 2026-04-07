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
    const pwd = document.getElementById('password');
    pwd.type = pwd.type === 'password' ? 'text' : 'password';
    this.classList.toggle('active');
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

document.getElementById("loginBtn").onclick = function() {

    var apiBase = (window.NanoBlog && typeof window.NanoBlog.apiBase === 'string')
        ? window.NanoBlog.apiBase
        : '';

    var layer = window.layer;

    var username = document.getElementById("username").value;
    var password = document.getElementById("password").value;
    var captcha = document.getElementById("captcha").value;

    if (!username || !password) {
        layer.msg("用户名和密码不能为空！", {icon: 5, shift: 6});
        return;
    }
    if (!captcha) {
        layer.msg("请输入验证码！", {icon: 5, shift: 6});
        return;
    }
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4 && xhr.status === 200) {
            var res = JSON.parse(xhr.responseText);
            if (res.code === 200) {
                sessionStorage.setItem("loginUsername", username);
                // 所有用户统一跳转首页
                layer.msg("登录成功！", {icon: 1, time: 1000}, function() {
                    window.location.href = apiBase + '/pages/front/index.html';
                });
            } else {
                layer.msg(res.msg, {icon: 2});
            }
        }
    };
    xhr.open("POST", apiBase + "/user/doLogin", true);
    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    xhr.send("username=" + encodeURIComponent(username) +
        "&password=" + encodeURIComponent(password) +
        "&captcha=" + encodeURIComponent(captcha));
};
