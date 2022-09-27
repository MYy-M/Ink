package com.myy.blog.service;

import com.myy.blog.vo.Result;
import com.myy.blog.vo.params.ArticleParam;
import com.myy.blog.vo.params.PageParams;

public interface ArticleService {

    /**
     * 分页查询，文章列表
     * @param pageParams
     * @return
     */

    Result ListArticle(PageParams pageParams);

    /**
     * 最热文章
     * @param limit
     * @return
     */

    Result hotArticle(int limit);

    /**
     *最新文章
     * @param limit
     * @return
     */
    Result newArticle(int limit);

    /**
     * 文章归档
     * @return
     */
    Result listArchives();

    /**
     * 查看文章详情
     * @param articleId
     * @return
     */
    Result findArticelById(Long articleId);

    /**
     * 发布文章
     * @param articleParam
     * @return
     */
    Result publish(ArticleParam articleParam);
}
