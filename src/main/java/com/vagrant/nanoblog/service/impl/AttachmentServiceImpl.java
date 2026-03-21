package com.vagrant.nanoblog.service.impl;

import com.vagrant.nanoblog.pojo.Attachment;
import com.vagrant.nanoblog.mapper.AttachmentMapper;
import com.vagrant.nanoblog.service.IAttachmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 附件表 服务实现类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@Service
public class AttachmentServiceImpl extends ServiceImpl<AttachmentMapper, Attachment> implements IAttachmentService {

}
