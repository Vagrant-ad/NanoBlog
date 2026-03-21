package com.vagrant.nanoblog.service.impl;

import com.vagrant.nanoblog.pojo.Category;
import com.vagrant.nanoblog.mapper.CategoryMapper;
import com.vagrant.nanoblog.service.ICategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 分类表 服务实现类
 * </p>
 *
 * @author vagrant
 * @since 2026-03-21
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

}
