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
    
    // ===================== 【后台管理相关方法】 =====================
    /** 管理员新增标签 */
    void addTag(Tag tag);
    
    /** 管理员软删除标签 */
    void deleteTagByAdmin(Long tagId);
}