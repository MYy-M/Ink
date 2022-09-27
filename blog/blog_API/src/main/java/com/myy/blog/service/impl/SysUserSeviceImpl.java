package com.myy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.myy.blog.dao.mapper.SysUserMapper;
import com.myy.blog.dao.pojo.SysUser;
import com.myy.blog.service.LoginService;
import com.myy.blog.service.SysUserService;
import com.myy.blog.vo.ErrorCode;
import com.myy.blog.vo.LoginUserVo;
import com.myy.blog.vo.Result;
import com.myy.blog.vo.UserVo;
import io.netty.util.internal.StringUtil;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SysUserSeviceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private LoginService loginService;

    @Override
    public UserVo findUserVoById(Long id) {
        SysUser sysUser = sysUserMapper.selectById(id);
        if(sysUser==null){
            sysUser=new SysUser();
            sysUser.setId(1L);
            sysUser.setAvatar("/static/img/logo.b3a48c0.png");
            sysUser.setNickname("myy");
        }
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(sysUser,userVo);
        return userVo;
    }

    @Override
    public SysUser findUserById(Long id) {
        SysUser sysUser = sysUserMapper.selectById(id);
        if(sysUser==null){
            sysUser=new SysUser();
            sysUser.setNickname("myy");
        }
        return sysUser;
    }

    @Override
    public SysUser findUser(String account, String password) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getAccount,account);
        queryWrapper.eq(SysUser::getPassword,password);
        queryWrapper.select(SysUser::getAccount,SysUser::getId,SysUser::getAvatar,SysUser::getNickname);
        queryWrapper.last("limit 1");
        return sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public Result findUserByToken(String token) {
        /**
         * 1、token合法性校验
         *    是否为空，解析是否成功 redis是否存在
         * 2、如果校验失败，返回错误
         * 3、如果成功，返回对应结果 loginUserVo
         */

        SysUser sysUser = loginService.checkToken(token);
        if(sysUser==null){
            return Result.fail(ErrorCode.TOKEN_ERROR.getCode(), ErrorCode.TOKEN_ERROR.getMsg());
        }
        LoginUserVo loginUserVo =new LoginUserVo();
        loginUserVo.setId(sysUser.getId());
        loginUserVo.setNickname(sysUser.getNickname());
        loginUserVo.setAvatar(sysUser.getAvatar());
        loginUserVo.setAccount(sysUser.getAccount());
        return Result.success(loginUserVo);
    }

    @Override
    public SysUser findUserByAccount(String account) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getAccount,account);
        queryWrapper.last("limit 1");
        return this.sysUserMapper.selectOne(queryWrapper);

    }

    @Override
    public void save(SysUser sysUser) {
        //保存用户这里 id会自动生成
        //默认生成的id是分布式id，采用了雪花算法
        this.sysUserMapper.insert(sysUser);
    }
}
