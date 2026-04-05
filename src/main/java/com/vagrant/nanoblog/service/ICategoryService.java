package com.vagrant.nanoblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vagrant.nanoblog.pojo.Category;
import com.vagrant.nanoblog.vo.CategoryTreeVO;
import java.util.List;

public interface ICategoryService extends IService<Category> {

    List<CategoryTreeVO> getCategoryTree();
    
    // ===================== 【后台管理相关方法】 =====================
    /** 管理员新增分类 */
    void addCategory(Category category);
    
    /** 管理员修改分类 */
    void updateCategory(Category category);
    
    /** 管理员软删除分类（如果该分类下还有文章，拒绝删除） */
    void deleteCategoryByAdmin(Long categoryId);
}