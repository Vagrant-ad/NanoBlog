package com.vagrant.nanoblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.pojo.Category;
import com.vagrant.nanoblog.service.ICategoryService;
import com.vagrant.nanoblog.vo.CategoryTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    @GetMapping("/list")
    public ResponseResult<List<Category>> list() {
        List<Category> list = categoryService.list(
                new QueryWrapper<Category>()
                        .eq("is_deleted", 0)
                        .orderByAsc("sort_order")
        );
        return ResponseResult.okResult(list);
    }

    @GetMapping("/tree")
    public ResponseResult<List<CategoryTreeVO>> getTree() {
        return ResponseResult.okResult(categoryService.getCategoryTree());
    }
}
