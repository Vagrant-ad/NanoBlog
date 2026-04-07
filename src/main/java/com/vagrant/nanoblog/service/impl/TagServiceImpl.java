package com.vagrant.nanoblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vagrant.nanoblog.mapper.TagMapper;
import com.vagrant.nanoblog.pojo.Tag;
import com.vagrant.nanoblog.service.ITagService;
import com.vagrant.nanoblog.vo.TagVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 标签服务实现
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements ITagService {

    @Override
    public List<TagVO> getTagsWithCount() {
        // 直接调用Mapper的SQL查询（已关联文章表统计数量）
        return baseMapper.getTagsWithCount();
    }

    // ===================== 【后台管理相关方法实现】 =====================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTag(Tag tag) {
        tag.setStatus(1);
        tag.setIsDeleted(0);
        tag.setCreateTime(LocalDateTime.now());
        this.save(tag);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTagByAdmin(Long tagId) {
        Tag tag = this.getById(tagId);
        if (tag == null) {
            throw new RuntimeException("标签不存在");
        }
        tag.setIsDeleted(1);
        tag.setUpdateTime(LocalDateTime.now());
        this.updateById(tag);
    }
}