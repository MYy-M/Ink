package com.myy.blog.service;

import com.myy.blog.dao.pojo.SysUser;
import com.myy.blog.vo.Result;
import com.myy.blog.vo.params.LoginParams;

public interface LoginService {
    /**
     * 登录功能
     *
     * @param loginParams
     * @return
     */
    Result login(LoginParams loginParams);

    SysUser checkToken(String token);

    /**
     * 退出登录
     *
     * @param token
     * @return
     */
    Result logout(String token);

    /**
     * 注册
     *
     * @param loginParams
     * @return
     */
    Result register(LoginParams loginParams);
}




