layui.use(['table', 'layer', 'form'], function(){
    var table = layui.table;
    var layer = layui.layer;
    var form = layui.form;
    var $ = layui.$;

    // 渲染表格
    var tableIns = table.render({
        elem: '#articleTable',
        url: '/admin/article/list',
        page: true,
        limit: 15,
        limits: [10, 15, 20, 30, 50],
        cols: [[
            {field: 'id', title: 'ID', width: 80, align: 'center'},
            {field: 'articleTitle', title: '标题', minWidth: 200, templet: function(d) {
                return '<span class="article-title" title="' + d.articleTitle + '">' + d.articleTitle + '</span>';
            }},
            {
                field: 'status',
                title: '状态',
                width: 100,
                align: 'center',
                templet: function(d) {
                    if (d.status === 0) {
                        return '<span class="status-badge status-draft">📝 草稿</span>';
                    } else if (d.status === 1) {
                        return '<span class="status-badge status-published">✅ 已发布</span>';
                    } else {
                        return '<span class="status-badge status-archived">📦 已归档</span>';
                    }
                }
            },
            {field: 'viewCount', title: '浏览量', width: 100, align: 'center', templet: function(d) {
                return '<span class="stat-number">👁️ ' + d.viewCount + '</span>';
            }},
            {field: 'likeCount', title: '点赞数', width: 100, align: 'center', templet: function(d) {
                return '<span class="stat-number">❤️ ' + d.likeCount + '</span>';
            }},
            {field: 'commentCount', title: '评论数', width: 100, align: 'center', templet: function(d) {
                return '<span class="stat-number">💬 ' + d.commentCount + '</span>';
            }},
            {field: 'createTime', title: '创建时间', width: 180, align: 'center'},
            {field: 'publishTime', title: '发布时间', width: 180, align: 'center'},
            {title: '操作', width: 180, toolbar: '#actionTpl', align: 'center'}
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
        var status = $('select[name="status"]').val();
        var title = $('input[name="title"]').val();
        tableIns.reload({
            where: { status: status, title: title },
            page: { curr: 1 }
        });
    });

    // 监听工具条
    table.on('tool(articleTable)', function(obj){
        var data = obj.data;
        var layEvent = obj.event;

        if(layEvent === 'archive'){
            layer.confirm('📦 确定要归档该文章吗？', {
                icon: 3,
                title: '归档确认',
                btn: ['确定', '取消']
            }, function(index){
                $.ajax({
                    url: '/admin/article/' + data.id + '/status',
                    type: 'PUT',
                    data: { status: 2 },
                    success: function(res) {
                        if (res.code === 200) {
                            layer.msg('✅ 归档成功', {icon: 1});
                            obj.update({ status: 2 });
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
            layer.confirm('⚠️ 确定要删除该文章吗？此操作不可恢复！', {
                icon: 3,
                title: '删除确认',
                btn: ['确定删除', '取消']
            }, function(index){
                $.ajax({
                    url: '/admin/article/' + data.id,
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
