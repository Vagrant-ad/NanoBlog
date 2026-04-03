package com.vagrant.nanoblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vagrant.nanoblog.pojo.Tag;
import com.vagrant.nanoblog.vo.TagVO;
import java.util.List;

/**
 * 标签 Mapper 接口
 */
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 查询所有标签（按文章数降序，用于热门标签云）
     */
    List<TagVO> getTagsWithCount();
}