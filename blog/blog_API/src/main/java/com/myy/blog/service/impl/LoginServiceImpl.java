package com.myy.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.myy.blog.dao.pojo.SysUser;
import com.myy.blog.service.LoginService;
import com.myy.blog.service.SysUserService;
import com.myy.blog.utils.JWTUtils;
import com.myy.blog.vo.ErrorCode;
import com.myy.blog.vo.Result;
import com.myy.blog.vo.params.LoginParams;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class LoginServiceImpl implements LoginService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;


    private static final String slat = "MYy!@#";

    @Override
    public Result login(LoginParams loginParams) {
        /**
         * 1、检查参数是否合法
         * 2、根据用户名和密码取User表中进行查询，是否存在
         * 3、如果不存在 就登陆失败
         * 4、如果存在就是用JWT生成token返回给前端
         * 5、token放入redis中，redis存储了token和user信息，同时存储了过期时间
         * （登录认证的时候，先认证token字符串是否合法，去redis认证是否存在）
         */

        String account = loginParams.getAccount();
        String  password =loginParams.getPassword();
        if(StringUtils.isBlank(account)||StringUtils.isBlank(password)){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), ErrorCode.PARAMS_ERROR.getMsg());
        }

        password = DigestUtils.md5Hex(password+slat);

        SysUser sysUser=  sysUserService.findUser(account,password);
        if(sysUser==null){
            return Result.fail(ErrorCode.ACCOUNT_PWD_NOT_EXIST.getCode(), ErrorCode.ACCOUNT_PWD_NOT_EXIST.getMsg());
        }
        String token = JWTUtils.createToken(sysUser.getId());
        redisTemplate.opsForValue().set("TOKEN_"+token, JSON.toJSONString(sysUser),1, TimeUnit.DAYS);
        return Result.success(token);
    }

    @Override
    public SysUser checkToken(String token) {
        if(StringUtils.isBlank(token)){
            return null;
        }
        Map<String,Object> stringObjectMap = JWTUtils.checkToken(token);
        if(stringObjectMap==null){
            return null;
        }
        String userJson = redisTemplate.opsForValue().get("TOKEN_"+token);
        if(StringUtils.isBlank(userJson)){
            return null;
        }
        SysUser sysUser = JSON.parseObject(userJson,SysUser.class);
        return sysUser;
    }

    @Override
    public Result logout(String token) {
        redisTemplate.delete("TOKEN_"+token);
        return Result.success(null);
    }

    @Override
    public Result register(LoginParams loginParams) {
        /**
         * 1、判断参数是否合法
         * 2、判断账户是否存在，存在返回账户已存在
         * 3、如果不存在，注册用户
         * 4、生成token
         * 5、存入redis，并返回
         * 6、注意 加上事务，一旦中间的任何过程出现问题，注册的用户需要回滚
         */
        String account = loginParams.getAccount();
        String password = loginParams.getPassword();
        String nickname = loginParams.getNickname();
        if (StringUtils.isBlank(account)
                || StringUtils.isBlank(password)
                || StringUtils.isBlank(nickname)
        ){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(),ErrorCode.PARAMS_ERROR.getMsg());
        }

        SysUser sysUser = sysUserService.findUserByAccount(account);
        if(sysUser!=null){
            return Result.fail(ErrorCode.ACCOUNT_EXIST.getCode(), ErrorCode.ACCOUNT_EXIST.getMsg());
        }

        sysUser = new SysUser();
        sysUser.setNickname(nickname);
        sysUser.setAccount(account);
        sysUser.setPassword(DigestUtils.md5Hex(password+slat));
        sysUser.setCreateDate(System.currentTimeMillis());
        sysUser.setLastLogin(System.currentTimeMillis());
        sysUser.setAvatar("/static/img/logo.b3a48c0.png");
        sysUser.setAdmin(1); //1 为true
        sysUser.setDeleted(0); // 0 为false
        sysUser.setSalt("");
        sysUser.setStatus("");
        sysUser.setEmail("");
        this.sysUserService.save(sysUser);
        String token = JWTUtils.createToken(sysUser.getId());
        //将token加入到redis中
        redisTemplate.opsForValue().set("TOKEN_"+token,JSON.toJSONString(sysUser),1, TimeUnit.DAYS);
        return Result.success(token);
    }
}
