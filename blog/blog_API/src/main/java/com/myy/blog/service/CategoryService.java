package com.myy.blog.service;


import com.myy.blog.vo.CategoryVo;
import com.myy.blog.vo.Result;

public interface CategoryService {

    /**
     * 查询文章类别
      * @param categoryId
     * @return
     */
    CategoryVo findCategoryById(Long categoryId);

    Result findAll();

    Result findAllDetail();

    Result categoryDetailById(Long id);
}
