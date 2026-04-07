layui.use(['form', 'layer', 'jquery', 'upload'], function() {
    const form = layui.form;
    const layer = layui.layer;
    const $ = layui.jquery;
    const upload = layui.upload;
    const EDIT_ID = NanoBlog.getQueryParam('id'); //有文章id则是编辑模式，无则是新建
    let easyMDE = null;
    let articleTags = [];

    init();

    function init() {
        //1.初始化UI组件
        initEditor();
        initTagSystem();
        initCoverUpload();

        //2.加载数据
        loadCategories();

        //3.绑定交互
        bindSubmit();
        if (EDIT_ID) {
            // 编辑模式：加载原文章数据回填
            loadArticleForEdit(EDIT_ID);
            // 修改页面标题提示
            document.title = '编辑文章 - NanoBlog';
        }
        //4.高亮导航栏“写文章”
        $('.navbar-right a[href*="editor.html"]').addClass('active');
    }

    function initEditor() {
        easyMDE = new EasyMDE({
            element: document.getElementById('markdown-editor'),
            spellChecker: false,
            autosave: {
                enabled: true,
                uniqueId: "nanoblog_draft_01",
                delay: 10000
            },
            placeholder: "请输入文本···",
            status: ["autosave", "lines", "words", "cursor"],
            toolbar: [
                "bold", "italic", "heading", "|",
                "quote", "code", "table", "unordered-list", "ordered-list", "|",
                "link", "image", "|",
                "preview", "side-by-side", "fullscreen", "|",
                "guide"
            ]
        });
    }


    function renderTagsPublic() {
        const $list = $('#tagList');
        $list.empty();
        articleTags.forEach(t => {
            $list.append(`<div class="tag-item" data-tag="${t}">${t}<i class="layui-icon layui-icon-close del-tag"></i></div>`);
        });
    }

    function initTagSystem() {
        const $input = $('#tagInput');
        const $list = $('#tagList');
        $input.on('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                const val = $(this).val().trim();
                if (val) {
                    if (articleTags.includes(val)) return layer.msg('标签已存在');
                    if (articleTags.length >= 5) return layer.msg('最多5个标签');
                    articleTags.push(val);
                    renderTagsPublic();
                    $(this).val('');
                }
            }
        });
        $list.on('click', '.del-tag', function() {
            const tag = $(this).parent().data('tag');
            articleTags = articleTags.filter(t => t !== tag);
            renderTagsPublic();
        });
    }

    function initCoverUpload() {
        upload.render({
            elem: '#coverUploadBtn',
            url: NanoBlog.apiBase + '/attachment/upload/image',
            accept: 'images',
            done: function(res) {
                if (res.code === 200 || res.code === 0) {
                    $('#coverPreview').attr('src', res.data).show();
                    $('#uploaderContent').hide();
                    $('#coverUrlInput').val(res.data);
                }
            }
        });
    }

    function bindSubmit() {
        //发布文章
        form.on('submit(publishBtn)', function(data) {
            const contentMd = easyMDE.value();
            if (!contentMd.trim()) return layer.msg('内容不能为空');
            submitArticle(data.field, 1);
            return false;
        });

        //存草稿:标题必填,正文可空
        $('.btn-draft').on('click', function() {
            const titleVal = $('input[name="articleTitle"]').val().trim();
            if (!titleVal) return layer.msg('请先输入文章标题');
            const field = {
                articleTitle: titleVal,
                articleSummary: $('textarea[name="articleSummary"]').val(),
                categoryId: $('#categorySelect').val()
            };
            submitArticle(field, 0);
        });
    }
    //公共提交流程,status:0草稿,1发布
    function submitArticle(field, status) {
        const submitData = {
            ...field,
            tags: articleTags,
            contentMd: easyMDE.value(),
            coverUrl: $('#coverUrlInput').val(), //封面图URL
            status: status
        };

        const loadIdx = layer.load(2);
        //根据是否有 EDIT_ID 决定走新建还是更新
        const url    = EDIT_ID ? (NanoBlog.apiBase + '/article/' + EDIT_ID) : (NanoBlog.apiBase + '/article/publish');
        const method = EDIT_ID ? 'PUT' : 'POST';
        $.ajax({
            url: url,
            type: method,
            contentType: 'application/json',
            data: JSON.stringify(submitData),
            success: function(res) {
                layer.close(loadIdx);
                if (res.code === 200) {
                    easyMDE.clearAutosavedValue();
                    if (status === 1) {
                        layer.msg('发布成功！', {icon: 1}, () => location.href = NanoBlog.apiBase + '/pages/front/index.html');
                    } else {
                        layer.msg('草稿已保存', {icon: 1});
                    }
                } else {
                    layer.msg(res.msg || '操作失败');
                }
            },
            error: function() {
                layer.close(loadIdx);
                layer.msg('网络异常，请重试');
            }
        });
    }
    //回填
    function loadArticleForEdit(id) {
        const loadIdx = layer.load(2);
        $.ajax({
            url: NanoBlog.apiBase + '/article/' + id,
            type: 'GET',
            success: function(res) {
                layer.close(loadIdx);
                if (res.code !== 200 || !res.data) {
                    layer.msg('文章加载失败');
                    return;
                }
                const data = res.data;

                // 回填标题
                $('input[name="articleTitle"]').val(data.title || '');

                // 回填摘要
                $('textarea[name="articleSummary"]').val(data.articleSummary || '');

                // 回填分类（等分类列表加载完再设置）
                // loadCategories 是异步的，用一个回调或延迟处理
                waitForCategories(function() {
                    $('#categorySelect').val(data.categoryId || '');
                    form.render('select');
                });

                // 回填标签
                articleTags = data.tags || [];
                renderTagsPublic(); // 见下方说明

                // 回填封面图
                if (data.coverImageUrl) {
                    $('#coverPreview').attr('src', data.coverImageUrl).show();
                    $('#uploaderContent').hide();
                    $('#coverUrlInput').val(data.coverImageUrl);
                }

                // 回填 Markdown 正文
                if (easyMDE && data.contentMd) {
                    easyMDE.value(data.contentMd);
                }
            },
            error: function() {
                layer.close(loadIdx);
                layer.msg('网络异常');
            }
        });
    }

    // 等分类下拉加载完成后执行回调
    // 因为 loadCategories 是异步的，需要轮询或用一个标志位
    let categoriesLoaded = false;
    let pendingCategoryCallback = null;

    function waitForCategories(cb) {
        if (categoriesLoaded) {
            cb();
        } else {
            pendingCategoryCallback = cb;
        }
    }
    function loadCategories() {
        $.ajax({
            url: NanoBlog.apiBase + '/category/list',
            type: 'GET',
            success: function(res) {
                if (res.code === 200 && res.data) {
                    res.data.forEach(function(c) {
                        $('#categorySelect').append(
                            '<option value="' + c.id + '">' + c.categoryName + '</option>'
                        );
                    });
                    form.render('select'); //通知layui重渲染下拉框
                    categoriesLoaded = true;
                    if (pendingCategoryCallback) {
                        pendingCategoryCallback();
                        pendingCategoryCallback = null;
                    }
                } else {
                    layer.msg('分类加载失败');
                }
            },
            error: function() {
                layer.msg('网络异常，分类加载失败');
            }
        });
    }
});