package com.vagrant.nanoblog.controller;


import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.pojo.Attachment;
import com.vagrant.nanoblog.pojo.User;
import com.vagrant.nanoblog.service.IAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * <p>
 * 附件表 前端控制器
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/attachment")
public class AttachmentController {
    private final IAttachmentService attachmentService;
    //获取登录用户id
    private Long getCurrentUserId(HttpSession session) {
        User loginUser = (User) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            throw new RuntimeException("请先登录");
        }
        return loginUser.getId();
    }

    /**
     * 图片上传接口
     * POST /upload/image
     * 前端 layui upload 组件调用此接口
     */

    @Value("${upload.root}")
    private String uploadRoot;

    @Value("${upload.urlPrefix}")
    private String uploadUrlPrefix;

    @PostMapping("/upload/image")
    public ResponseResult<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request, HttpSession session) {

        // 1. 基本校验
        if (file == null || file.isEmpty()) {
            return ResponseResult.errorResult(400, "文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            return ResponseResult.errorResult(400, "文件名异常");
        }

        // 2. 只允许图片类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseResult.errorResult(400, "只允许上传图片文件");
        }

        // 3. 构建存储路径：webapp/static/uploads/2026/03/
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String uploadDir = uploadRoot  + datePath + "/";

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs(); // 目录不存在则创建
        }

        // 4. 生成唯一文件名，保留原始扩展名
        String ext = originalName.substring(originalName.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString().replace("-", "") + ext;
        File destFile = new File(uploadDir + newFileName);

        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            return ResponseResult.errorResult(500, "文件保存失败：" + e.getMessage());
        }

        // 5. 拼接可访问的 URL
        String fileUrl = uploadUrlPrefix + datePath + "/" + newFileName;

        // 6. 写 attachment 表记录（userId 暂时硬编码，后续替换）
        Attachment attachment = new Attachment();
        attachment.setUploaderId(getCurrentUserId(session)); //获取当前用户id
        attachment.setFileName(originalName);
        attachment.setFileUrl(fileUrl);
        attachment.setFileType(contentType);
        attachment.setFileSize(file.getSize());
        attachmentService.save(attachment);

        // 7. 返回 URL 给前端，前端存入 coverUrlInput
        return ResponseResult.okResult(fileUrl);
    }
}
