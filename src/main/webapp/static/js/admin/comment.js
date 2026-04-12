layui.use(['table', 'layer', 'form'], function(){
    var table = layui.table;
    var layer = layui.layer;
    var form = layui.form;
    var $ = layui.$;

    // 渲染表格
    var tableIns = table.render({
        elem: '#commentTable',
        url: '/admin/comment/list',
        page: true,
        limit: 15,
        limits: [10, 15, 20, 30, 50],
        cols: [[
            {field: 'id', title: 'ID', width: 80, align: 'center'},
            {field: 'authorNickname', title: '评论者', width: 120, align: 'center'},
            {field: 'articleTitle', title: '所属文章', minWidth: 200, templet: '#articleTpl'},
            {field: 'commentContent', title: '评论内容', minWidth: 250, templet: '#contentTpl'},
            {field: 'createTime', title: '评论时间', width: 180, align: 'center'},
            {
                field: 'status',
                title: '状态',
                width: 100,
                align: 'center',
                templet: function(d) {
                    if (d.status === 1) {
                        return '<span class="status-badge status-normal">✓ 正常</span>';
                    } else {
                        return '<span class="status-badge status-disabled">✕ 禁用</span>';
                    }
                }
            },
            {title: '操作', width: 120, toolbar: '#actionTpl', align: 'center'}
        ]],
        request: {
            pageName: 'page',
            limitName: 'size'
        },
        response: {
            statusCode: 200
        },
        parseData: function(res) {
            return {
                "code": res.code,
                "msg": res.msg,
                "count": res.data.total,
                "data": res.data.records
            };
        }
    });

    // 搜索
    $('#searchBtn').click(function() {
        var articleId = $('input[name="articleId"]').val();
        tableIns.reload({
            where: { articleId: articleId },
            page: { curr: 1 }
        });
    });

    // 监听工具条
    table.on('tool(commentTable)', function(obj){
        var data = obj.data;
        var layEvent = obj.event;

        if(layEvent === 'delete'){
            layer.confirm('⚠️ 确定要删除该评论吗？此操作不可恢复！', {
                icon: 3,
                title: '删除确认',
                btn: ['确定删除', '取消']
            }, function(index){
                $.ajax({
                    url: '/admin/comment/' + data.id,
                    type: 'DELETE',
                    success: function(res) {
                        if (res.code === 200) {
                            layer.msg('✅ 删除成功', {icon: 1});
                            obj.del();
                        } else {
                            layer.msg(res.msg || '❌ 删除失败', {icon: 2});
                        }
                    },
                    error: function() {
                        layer.msg('❌ 网络错误', {icon: 2});
                    }
                });
                layer.close(index);
            });
        } else if(layEvent === 'viewContent') {
            layer.open({
                type: 1,
                title: '📝 评论详情',
                area: ['600px', '400px'],
                content: '<div style="padding: 20px; line-height: 1.8;">' + data.commentContent + '</div>',
                shadeClose: true
            });
        }
    });

    // 退出登录
    $('#logout').click(function() {
        layer.confirm('🚪 确定要退出登录吗？', {
            icon: 3,
            title: '提示',
            btn: ['确定', '取消']
        }, function(index){
            $.post('/user/logout', function(res) {
                if (res.code === 200) {
                    window.location.href = '/pages/front/login.html';
                }
            });
            layer.close(index);
        });
    });
});
