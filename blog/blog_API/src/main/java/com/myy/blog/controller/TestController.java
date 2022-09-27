package com.myy.blog.controller;


import com.myy.blog.dao.pojo.SysUser;
import com.myy.blog.utils.UserThreadLocal;
import com.myy.blog.vo.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {

    @RequestMapping
    public Result test(){
        SysUser sysUser = UserThreadLocal.get();
        System.out.println(sysUser);
        return Result.success(null);
    }
}
