layui.use(['table', 'layer', 'form'], function(){
    var table = layui.table;
    var layer = layui.layer;
    var form = layui.form;
    var $ = layui.$;

    // 渲染表格
    var tableIns = table.render({
        elem: '#userTable',
        url: '/admin/user/list',
        page: true,
        limit: 15,
        limits: [10, 15, 20, 30, 50],
        cols: [[
            {field: 'id', title: 'ID', width: 80, align: 'center'},
            {field: 'username', title: '用户名', width: 150, align: 'center', templet: function(d) {
                    return '<span class="username-text">' + d.username + '</span>';
                }},
            {field: 'nickname', title: '昵称', width: 150, align: 'center'},
            {field: 'email', title: '邮箱', width: 200, templet: function(d) {
                    return '<span class="email-text">' + d.email + '</span>';
                }},
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
            {field: 'createTime', title: '注册时间', width: 180, align: 'center'},
            {title: '操作', width: 200, toolbar: '#actionTpl', align: 'center'}
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
        var username = $('input[name="username"]').val();
        tableIns.reload({
            where: { username: username },
            page: { curr: 1 }
        });
    });

    // 监听工具条
    table.on('tool(userTable)', function(obj){
        var data = obj.data;
        var layEvent = obj.event;

        if(layEvent === 'toggleStatus'){
            var newStatus = data.status === 1 ? 0 : 1;
            var statusText = newStatus === 1 ? '启用' : '禁用';
            layer.confirm('⚠️ 确定要' + statusText + '该用户吗？', {
                icon: 3,
                title: '操作确认',
                btn: ['确定', '取消']
            }, function(index){
                $.ajax({
                    url: '/admin/user/' + data.id + '/status',
                    type: 'PUT',
                    data: { status: newStatus },
                    success: function(res) {
                        if (res.code === 200) {
                            layer.msg('✅ ' + statusText + '成功', {icon: 1});
                            obj.update({ status: newStatus });
                        } else {
                            layer.msg('❌ ' + (res.msg || '操作失败'), {icon: 2});
                        }
                    },
                    error: function() {
                        layer.msg('❌ 网络错误', {icon: 2});
                    }
                });
                layer.close(index);
            });
        } else if(layEvent === 'delete'){
            layer.confirm('⚠️ 确定要删除该用户吗？此操作不可恢复！', {
                icon: 3,
                title: '删除确认',
                btn: ['确定删除', '取消']
            }, function(index){
                $.ajax({
                    url: '/admin/user/' + data.id,
                    type: 'DELETE',
                    success: function(res) {
                        if (res.code === 200) {
                            layer.msg('✅ 删除成功', {icon: 1});
                            obj.del();
                        } else {
                            layer.msg('❌ ' + (res.msg || '删除失败'), {icon: 2});
                        }
                    },
                    error: function() {
                        layer.msg('❌ 网络错误', {icon: 2});
                    }
                });
                layer.close(index);
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