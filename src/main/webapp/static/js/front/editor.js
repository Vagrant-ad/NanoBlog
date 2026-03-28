layui.use(['form', 'layer', 'jquery', 'upload'], function() {
    const form = layui.form;
    const layer = layui.layer;
    const $ = layui.jquery;
    const upload = layui.upload;

    let easyMDE = null;
    let articleTags = [];

    init();

    function init() {
        // 1. 初始化 UI 组件
        initEditor();
        initTagSystem();
        initCoverUpload();

        // 2. 加载数据
        loadCategories();

        // 3. 绑定交互
        bindSubmit();

        // 4. 激活导航栏“写文章”按钮的高亮态
        // 只需要这一行，样式会自动从 common.css 加载
        $('.navbar-right a[href*="editor.html"]').addClass('active');
    }

    // ... 后面 initEditor, initTagSystem 等逻辑保持不变 ...

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
                    renderTags();
                    $(this).val('');
                }
            }
        });
        $list.on('click', '.del-tag', function() {
            const tag = $(this).parent().data('tag');
            articleTags = articleTags.filter(t => t !== tag);
            renderTags();
        });
        function renderTags() {
            $list.empty();
            articleTags.forEach(t => {
                $list.append(`<div class="tag-item" data-tag="${t}">${t}<i class="layui-icon layui-icon-close del-tag"></i></div>`);
            });
        }
    }

    function initCoverUpload() {
        upload.render({
            elem: '#coverUploadBtn',
            url: '/attachment/upload/image',
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
        // 发布文章
        form.on('submit(publishBtn)', function(data) {
            const contentMd = easyMDE.value();
            if (!contentMd.trim()) return layer.msg('内容不能为空');
            submitArticle(data.field, 1);
            return false;
        });

        // 存为草稿：标题有值即可保存，内容允许为空
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
    // 公共提交函数，status: 0=草稿 1=发布
    function submitArticle(field, status) {
        const submitData = {
            ...field,
            tags: articleTags,
            contentMd: easyMDE.value(),
            coverUrl: $('#coverUrlInput').val(),  // 封面图 URL
            status: status
        };

        const loadIdx = layer.load(2);
        $.ajax({
            url: '/article/publish',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(submitData),
            success: function(res) {
                layer.close(loadIdx);
                if (res.code === 200) {
                    easyMDE.clearAutosavedValue();
                    if (status === 1) {
                        layer.msg('发布成功！', {icon: 1}, () => location.href = '/pages/front/index.html');
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
    function loadCategories() {
        $.ajax({
            url: '/category/list',
            type: 'GET',
            success: function(res) {
                if (res.code === 200 && res.data) {
                    res.data.forEach(function(c) {
                        $('#categorySelect').append(
                            '<option value="' + c.id + '">' + c.categoryName + '</option>'
                        );
                    });
                    form.render('select'); // 通知 layui 重新渲染下拉框
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