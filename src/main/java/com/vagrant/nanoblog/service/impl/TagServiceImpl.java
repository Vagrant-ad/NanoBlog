package com.vagrant.nanoblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vagrant.nanoblog.mapper.TagMapper;
import com.vagrant.nanoblog.pojo.Tag;
import com.vagrant.nanoblog.service.ITagService;
import com.vagrant.nanoblog.vo.TagVO;
import org.springframework.stereotype.Service;
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
}