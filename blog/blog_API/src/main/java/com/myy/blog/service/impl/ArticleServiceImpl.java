package com.myy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myy.blog.dao.dos.Archives;
import com.myy.blog.dao.mapper.ArticleBodyMapper;
import com.myy.blog.dao.mapper.ArticleMapper;
import com.myy.blog.dao.mapper.ArticleTagMapper;
import com.myy.blog.dao.pojo.Article;
import com.myy.blog.dao.pojo.ArticleBody;
import com.myy.blog.dao.pojo.ArticleTag;
import com.myy.blog.dao.pojo.SysUser;
import com.myy.blog.service.*;
import com.myy.blog.utils.UserThreadLocal;
import com.myy.blog.vo.ArticleBodyVo;
import com.myy.blog.vo.ArticleVo;
import com.myy.blog.vo.Result;
import com.myy.blog.vo.TagVo;
import com.myy.blog.vo.params.ArticleParam;
import com.myy.blog.vo.params.PageParams;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private TagService tagService;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Autowired
    private SysUserService sysUserSevice;



    @Override
    public Result ListArticle(PageParams pageParams) {
        Page<Article> page = new Page<>(pageParams.getPage(),pageParams.getPageSize());

        IPage<Article> articleIPage = articleMapper.listArticle(
                page,
                pageParams.getCategoryId(),
                pageParams.getTagId(),
                pageParams.getYear(),
                pageParams.getMonth());

        List<Article> records = articleIPage.getRecords();
        return Result.success(copyList(records,true,true));

    }


//    @Override
//    public Result ListArticle(PageParams pageParams) {
//        /**
//         * 1、分页查询article数据表
//         */
//
//        Page<Article> page = new Page<>(pageParams.getPage(),pageParams.getPageSize());
//        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
//        if(pageParams.getCategoryId() != null){
//            queryWrapper.eq(Article::getCategoryId,pageParams.getCategoryId());
//        }
//        List<Long> articleIdList = new ArrayList<>();
//        if(pageParams.getTagId() != null){
//            //article表中并没有tag字段，一篇文章有多个标签
//            //article_tag  article_id 1 : n tag_id
//            LambdaQueryWrapper<ArticleTag> articleTagLambdaQueryWrapper = new LambdaQueryWrapper<>();
//            articleTagLambdaQueryWrapper.eq(ArticleTag::getTagId,pageParams.getTagId());
//            List<ArticleTag> articleTags = articleTagMapper.selectList(articleTagLambdaQueryWrapper);
//            for (ArticleTag articleTag : articleTags) {
//                articleIdList.add(articleTag.getArticleId());
//            }
//            if(articleIdList.size()>0){
//                //id in(1,2,3)
//                queryWrapper.in(Article::getId,articleIdList);
//            }
//        }
//        queryWrapper.orderByDesc(Article::getWeight,Article::getCreateDate);
//        Page<Article> articlePage= articleMapper.selectPage(page,queryWrapper);
//        List<Article> records = articlePage.getRecords();
//        //可以直接返回List?
//        List<ArticleVo> articleVoList = copyList(records,true,true);
//        return Result.success(articleVoList);
//    }



    @Override
    public Result hotArticle(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit "+limit);
        //select id,title from article order by view_counts desc limit 5
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles,false,false));
    }

    @Override
    public Result newArticle(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getCreateDate);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit "+limit);
        //select id,title from article order by create_date desc limit 5
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles,false,false));
    }

    @Override
    public Result listArchives() {
        List<Archives> archivesList = articleMapper.listArchives();
        return Result.success(archivesList);
    }

    @Autowired
    private ThreadService threadService;

    @Override
    public Result findArticelById(Long articleId) {
        /**
         * 1、根据id查询文章信息
         * 2、根据bodyId和categoryId做关联查询
         */
        Article article = this.articleMapper.selectById(articleId);
        ArticleVo articleVo = copy(article,true,true,true,true);
        //查看完文章，新增阅读数
        //有问题！！！！查看文章本应该直接返回数据，这个时候做了一个更新操作，更新时加写锁，阻塞其他的读操作，性能比较低
        //更新增加了此次接口的耗时
        //一旦更新出问题不能影响查看文章的操作
        //线程池：可以把更新操作放到线程池中取执行，这样就跟主线程不相关了
       threadService.updateArticleViewCount(articleMapper,article);
        return Result.success(articleVo);
    }

    @Override
    public Result publish(ArticleParam articleParam) {
        //此接口要加入到登录拦截当中
        SysUser sysUser = UserThreadLocal.get();

        /**
         * 1、发布文章  构建Article对象
         * 2、作者id 当前的登录用户
         * 3、标签 要将标签加入到关联表中
         * 4、内容存储 article BodyId
         */
        Article article = new Article();
        article.setAuthorId(sysUser.getId());
        article.setWeight(Article.Article_Common);
        article.setViewCounts(0);
        article.setTitle(articleParam.getTitle());
        article.setSummary(articleParam.getSummary());
        article.setCommentCounts(0);
        article.setCreateDate(System.currentTimeMillis());
        article.setCategoryId(articleParam.getCategory().getId());
        //插入之后会生成一个文章id
        this.articleMapper.insert(article);
        //tag
        List<TagVo> tags = articleParam.getTags();
        if(tags!=null){
            for (TagVo tag : tags) {
                Long articleId = article.getId();
                ArticleTag articleTag = new ArticleTag();
                articleTag.setTagId(tag.getId());
                articleTag.setArticleId(articleId);
                articleTagMapper.insert(articleTag);
            }

        }
        //body
        ArticleBody articleBody = new ArticleBody();
        articleBody.setArticleId(article.getId());
        articleBody.setContent(articleParam.getBody().getContent());
        articleBody.setContentHtml(articleParam.getBody().getContentHtml());
        articleBodyMapper.insert(articleBody);
        article.setBodyId(articleBody.getId());
        articleMapper.updateById(article);
        HashMap<String,String> map = new HashMap<>();
        map.put("id",article.getId().toString());

        return Result.success(map);
    }

    private List<ArticleVo> copyList(List<Article> records,boolean isTag,boolean isAuthor) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record,isTag,isAuthor,false,false));
        }
        return articleVoList;
    }

    private List<ArticleVo> copyList(List<Article> records,boolean isTag,boolean isAuthor,boolean isBody,boolean isCategory) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record,isTag,isAuthor,isBody,isCategory));
        }
        return articleVoList;
    }

    @Autowired
    private CategoryService categoryService;

    private ArticleVo copy(Article article,boolean isTag,boolean isAuthor,boolean isBody,boolean isCategory){
        ArticleVo articleVo =new ArticleVo();
        BeanUtils.copyProperties(article,articleVo);

        articleVo.setCreateDate(new DateTime(article.getCreateDate()).toString("yyyy-MM-dd HH:mm"));
        //并不是所有的接口都需要标签、作者信息

        if(isTag){
            Long articleId = article.getId();
            articleVo.setTags(tagService.findTagsByArticleId(articleId));
        }
        if(isAuthor){
            Long authorId = article.getAuthorId();
            articleVo.setAuthor(sysUserSevice.findUserById(authorId).getNickname());
        }
        if(isBody){
            Long bodyId =article.getBodyId();
            articleVo.setBody(findArticelBodyById(bodyId));
        }
        if(isCategory){
            Long categoryId = article.getCategoryId();
            articleVo.setCategory(categoryService.findCategoryById(categoryId));
        }

        return articleVo;
    }

    @Autowired
    private ArticleBodyMapper articleBodyMapper;

    private ArticleBodyVo findArticelBodyById(Long bodyId) {
        ArticleBody articleBody = articleBodyMapper.selectById(bodyId);
        ArticleBodyVo articleBodyVo = new ArticleBodyVo();
        articleBodyVo.setContent(articleBody.getContent());
        return articleBodyVo;
    }
}
