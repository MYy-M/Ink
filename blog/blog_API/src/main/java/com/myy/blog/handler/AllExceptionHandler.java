package com.myy.blog.handler;


import com.myy.blog.vo.Result;
import jdk.nashorn.internal.objects.NativeError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//对加了@ControllerService注解的方法进行拦截处理AOP的实现
@ControllerAdvice

public class AllExceptionHandler {
    //进行异常处理，处理Exception.class的异常
    @ExceptionHandler(Exception.class)
    @ResponseBody//返回JSON数据
    public Result doException(Exception ex){
        ex.printStackTrace();
        return Result.fail(-100,"系统异常");
    }
}
