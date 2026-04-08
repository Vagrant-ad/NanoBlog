package com.vagrant.nanoblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vagrant.nanoblog.mapper.ArticleMapper;
import com.vagrant.nanoblog.mapper.CategoryMapper;
import com.vagrant.nanoblog.pojo.Category;
import com.vagrant.nanoblog.service.ICategoryService;
import com.vagrant.nanoblog.vo.CategoryTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    private final ArticleMapper articleMapper;

    @Override
    public List<CategoryTreeVO> getCategoryTree() {
        QueryWrapper<Category> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.eq("is_deleted", 0);
        wrapper.orderByAsc("sort_order");

        List<Category> allCategory = list(wrapper);

        List<CategoryTreeVO> parentList = allCategory.stream()
                .filter(c -> c.getParentId() == 0)
                .map(this::convert)
                .collect(Collectors.toList());

        Map<Long, List<CategoryTreeVO>> childMap = allCategory.stream()
                .filter(c -> c.getParentId() != 0)
                .collect(Collectors.groupingBy(
                        Category::getParentId,
                        Collectors.mapping(this::convert, Collectors.toList())
                ));

        parentList.forEach(p -> p.setChildren(childMap.getOrDefault(p.getId(), new ArrayList<>())));

        return parentList;
    }

    private CategoryTreeVO convert(Category category) {
        CategoryTreeVO vo = new CategoryTreeVO();
        vo.setId(category.getId());
        vo.setCategoryName(category.getCategoryName());
        vo.setCategorySlug(category.getCategorySlug());
        vo.setSortOrder(category.getSortOrder());
        return vo;
    }

    // ===================== 【后台管理相关方法实现】 =====================
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCategory(Category category) {
        category.setStatus(1);
        category.setIsDeleted(0);
        category.setCreateTime(LocalDateTime.now());
        this.save(category);
    }

    // ... existing code ...

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Category category) {
        Category existing = this.getById(category.getId());
        if (existing == null) {
            throw new RuntimeException("分类不存在");
        }

        if (category.getParentId() != null && !category.getParentId().equals(existing.getParentId())) {
            if (category.getParentId().equals(category.getId())) {
                throw new RuntimeException("不能将分类设置为自己的子分类");
            }

            if (isDescendant(category.getId(), category.getParentId())) {
                throw new RuntimeException("不能将父分类设置为自己的子分类");
            }
        }

        existing.setParentId(category.getParentId() != null ? category.getParentId() : 0);
        existing.setCategoryName(category.getCategoryName());
        existing.setSortOrder(category.getSortOrder());
        existing.setUpdateTime(LocalDateTime.now());
        this.updateById(existing);
    }

    private boolean isDescendant(Long parentId, Long childId) {
        Category child = this.getById(childId);
        if (child == null || child.getParentId() == null || child.getParentId() == 0) {
            return false;
        }
        if (child.getParentId().equals(parentId)) {
            return true;
        }
        return isDescendant(parentId, child.getParentId());
    }

// ... existing code ...


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategoryByAdmin(Long categoryId) {
        Category category = this.getById(categoryId);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        
        // 检查该分类下是否有文章
        long count = articleMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.vagrant.nanoblog.pojo.Article>()
                .eq("category_id", categoryId)
                .eq("is_deleted", 0)
        );
        
        if (count > 0) {
            throw new RuntimeException("该分类下还有文章，请先移除该分类下的所有文章");
        }
        
        category.setIsDeleted(1);
        category.setUpdateTime(LocalDateTime.now());
        this.updateById(category);
    }
}