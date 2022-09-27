package com.myy.blog.controller;


import com.myy.blog.common.aop.LogAnnotation;
import com.myy.blog.common.cache.Cache;
import com.myy.blog.service.ArticleService;
import com.myy.blog.vo.Result;
import com.myy.blog.vo.params.ArticleParam;
import com.myy.blog.vo.params.PageParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//JSON数据进行交互
@RestController
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * 首页文章列表
     * @param pageParams
     * @return
     */

    @PostMapping
    @LogAnnotation(module="文章",operator="获取文章列表")
    @Cache(expire = 5 * 60 * 1000,name = "listArticle")
    public Result ListArticle(@RequestBody PageParams pageParams){

        //int i = 10/0;
        return articleService.ListArticle(pageParams);
    }

    /**
     * 首页最热文章
     * @return
     */
    @PostMapping("/hot")
    @Cache(expire = 5 * 60 * 1000,name = "hot_article")
    public Result hotArticle(){
        int limit = 5;
        return articleService.hotArticle(limit);
    }

    /**
     * 最新文章
     * @return
     */
    @PostMapping("/new")
    @Cache(expire = 5 * 60 * 1000,name = "news_article")
    public Result newArticle(){
        int limit = 5;
        return articleService.newArticle(limit);
    }

    /**
     * 文章归档
     * @return
     */
    @PostMapping("/listArchives")
    public Result listArchives(){
        return articleService.listArchives();
    }

    /**
     * 文章详情
     * @param articleId
     * @return
     */
    @PostMapping("view/{id}")
    public Result findArticleById(@PathVariable("id") Long articleId){
        return articleService.findArticelById(articleId);
    }

    @PostMapping("publish")
    public Result publish(@RequestBody ArticleParam articleParam){
        return articleService.publish(articleParam);
    }
}
