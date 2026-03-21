package com.vagrant.nanoblog.service.impl;

import com.vagrant.nanoblog.pojo.Tag;
import com.vagrant.nanoblog.mapper.TagMapper;
import com.vagrant.nanoblog.service.ITagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 标签表 服务实现类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements ITagService {

}
