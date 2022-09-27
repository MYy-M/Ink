package com.myy.blog.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myy.blog.dao.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentsMapper extends BaseMapper<Comment> {
}
