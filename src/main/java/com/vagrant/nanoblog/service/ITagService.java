package com.vagrant.nanoblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vagrant.nanoblog.pojo.Tag;
import com.vagrant.nanoblog.vo.TagVO;
import java.util.List;

/**
 * 标签服务接口
 */
public interface ITagService extends IService<Tag> {

    /**
     * 查询所有标签（含文章数，热门排序）
     */
    List<TagVO> getTagsWithCount();
}