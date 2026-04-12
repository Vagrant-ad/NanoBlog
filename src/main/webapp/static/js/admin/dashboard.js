layui.use(['element', 'layer'], function(){
    var element = layui.element;
    var layer = layui.layer;
    var $ = layui.$;

    //显示当前日期
    var now = new Date();
    var dateStr = now.getFullYear() + '年' +
                 (now.getMonth() + 1) + '月' +
                 now.getDate() + '日 ' +
                 ['星期日','星期一','星期二','星期三','星期四','星期五','星期六'][now.getDay()];
    $('#currentDate').text(dateStr);

    //加载统计数据
    loadStats();

    function loadStats() {
        $.ajax({
            url: '/admin/stats',
            type: 'GET',
            success: function(res) {
                if (res.code === 200) {
                    animateNumber('userCount', res.data.userCount);
                    animateNumber('articleCount', res.data.articleCount);
                    animateNumber('commentCount', res.data.commentCount);
                    animateNumber('todayArticleCount', res.data.todayArticleCount);
                } else {
                    layer.msg(res.msg || '加载失败');
                }
            },
            error: function() {
                layer.msg('网络错误');
            }
        });
    }

    //数字动画
    function animateNumber(elementId, targetNumber) {
        var element = $('#' + elementId);
        var duration = 1000;
        var start = 0;
        var increment = targetNumber / (duration / 16);
        var current = start;

        var timer = setInterval(function() {
            current += increment;
            if (current >= targetNumber) {
                current = targetNumber;
                clearInterval(timer);
            }
            element.text(Math.floor(current));
        }, 16);
    }

    //退出登录
    $('#logout').click(function() {
        layer.confirm('🚪 确定要返回前台首页吗？', {
            icon: 3,
            title: '提示',
            btn: ['确定', '取消']
        }, function(index){
            window.location.href = '/index.jsp';
            layer.close(index);
        });
    });
});
