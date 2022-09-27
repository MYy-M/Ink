package com.myy.blog.service;

import com.myy.blog.vo.Result;
import com.myy.blog.vo.TagVo;

import java.util.List;

public interface TagService {
  List<TagVo>  findTagsByArticleId(Long articleId);

   Result hots(int limit);

    Result findAll();

    Result findAllDetail();

    Result findAllDetailById(Long id);
}
