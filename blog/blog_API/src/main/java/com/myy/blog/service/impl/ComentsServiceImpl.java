package com.myy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.myy.blog.dao.mapper.CommentsMapper;
import com.myy.blog.dao.pojo.Article;
import com.myy.blog.dao.pojo.Comment;
import com.myy.blog.dao.pojo.SysUser;
import com.myy.blog.service.CommentsService;
import com.myy.blog.service.SysUserService;
import com.myy.blog.utils.UserThreadLocal;
import com.myy.blog.vo.CommentVo;
import com.myy.blog.vo.Result;
import com.myy.blog.vo.UserVo;
import com.myy.blog.vo.params.CommentParam;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.List;

@Service
public class ComentsServiceImpl implements CommentsService {

    @Autowired
    private CommentsMapper commentsMapper;

    @Autowired
    private SysUserService sysUserService;


    @Override
    public Result commentsByArticleId(Long id) {
        /**
         * 1、根据文章id从comments表查询评论列表
         * 2、根据作者id查询作者的信息
         * 3、判断 如果level=1，要去查询它有没有子评论
         * 4、如果有根据parent_id进行查询
         */
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getArticleId,id);
        queryWrapper.eq(Comment::getLevel,1);
        List<Comment> comments = commentsMapper.selectList(queryWrapper);
        List<CommentVo> commentVoList = copyList(comments);
        return Result.success(commentVoList);
    }

    @Override
    public Result comment(CommentParam commentParam) {
        SysUser sysUser = UserThreadLocal.get();
        Comment comment = new Comment();
        comment.setArticleId(commentParam.getArticleId());
        comment.setAuthorId(sysUser.getId());
        comment.setContent(commentParam.getContent());
        comment.setCreateDate(System.currentTimeMillis());
        Long parent = commentParam.getParent();
        if (parent == null || parent == 0) {
            comment.setLevel(1);
        }else{
            comment.setLevel(2);
        }
        comment.setParentId(parent == null ? 0 : parent);
        Long toUserId = commentParam.getToUserId();
        comment.setToUid(toUserId == null ? 0 : toUserId);
        this.commentsMapper.insert(comment);

        CommentVo commentVo = copy(comment);
        return Result.success(commentVo);
    }

    private List<CommentVo> copyList(List<Comment> comments) {
        List<CommentVo> commentVoList = new ArrayList<>();
        for (Comment comment : comments) {
            commentVoList.add(copy(comment));
        }
        return commentVoList;
    }

    private CommentVo copy(Comment comment) {
        CommentVo commentVo = new CommentVo();
        BeanUtils.copyProperties(comment,commentVo);
        //作者信息
        Long authorId = comment.getAuthorId();
        UserVo userVo = this.sysUserService.findUserVoById(authorId);
        commentVo.setAuthor(userVo);
        //子评论
        Integer level = comment.getLevel();
        if(1==level){
            Long id = comment.getId();
            List<CommentVo> commentVoList = findCommentsByParentId(id);
            commentVo.setChildrens(commentVoList);
        }
        //to User 给谁评论
        if(level>1){
            Long toUid = comment.getToUid();
            UserVo toUserVo = this.sysUserService.findUserVoById(toUid);
            commentVo.setToUser(toUserVo);
        }

        return commentVo;
    }

    private List<CommentVo> findCommentsByParentId(Long id) {
        LambdaQueryWrapper<Comment> queryWrapper= new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getParentId,id);
        queryWrapper.eq(Comment::getLevel,2);

        return copyList(commentsMapper.selectList(queryWrapper));
    }
}
