package com.myy.blog.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myy.blog.dao.pojo.Tag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 根据文章Id,查询标签列表
     * @param articleId
     * @return
     */
    List<Tag> findTagsByArticleId(Long articleId);


    /**
     *查询最热标签，前n条
     * @param limit
     * @return
     */
    List<Long> findHotsTagIds(int limit);

    List<Tag> findTagsByTagsIds(List<Long> tagIds);
}
