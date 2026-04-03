package com.vagrant.nanoblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vagrant.nanoblog.mapper.CategoryMapper;
import com.vagrant.nanoblog.pojo.Category;
import com.vagrant.nanoblog.service.ICategoryService;
import com.vagrant.nanoblog.vo.CategoryTreeVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

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
}