package com.vagrant.nanoblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vagrant.nanoblog.pojo.Category;
import com.vagrant.nanoblog.vo.CategoryTreeVO;
import java.util.List;

public interface ICategoryService extends IService<Category> {

    List<CategoryTreeVO> getCategoryTree();
}