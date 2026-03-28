package com.vagrant.nanoblog.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vagrant.nanoblog.common.ResponseResult;
import com.vagrant.nanoblog.pojo.Category;
import com.vagrant.nanoblog.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 分类表 前端控制器
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    /**
     * 获取所有启用的分类（供编辑器下拉选择）
     * GET /category/list
     */
    @GetMapping("/list")
    public ResponseResult<List<Category>> list() {
        List<Category> list = categoryService.list(
                new QueryWrapper<Category>()
                        .eq("status", 1)
                        .eq("is_deleted", 0)
                        .orderByAsc("sort_order")
        );
        return ResponseResult.okResult(list);
    }

    /**
     * 获取树形分类（父分类 + 子分类，供导航栏展示）
     * GET /category/tree
     * 后续如需要层级展示时使用，现在可以先不实现
     */
    // TODO: getTree()
}
