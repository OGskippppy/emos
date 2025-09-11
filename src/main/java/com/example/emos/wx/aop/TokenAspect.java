package com.example.emos.wx.aop;


import com.example.emos.wx.config.shiro.TreadLocalToken;
import com.example.emos.wx.pojo.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.aspectj.lang.annotation.Aspect;

import java.util.Map;
import java.util.HashMap;

@Component
@Aspect
public class TokenAspect {
    @Autowired
    private TreadLocalToken treadLocalToken;

    @Pointcut("execution(* com.example.emos.wx.controller.*.*(..))")
    public void aspect() {

    }

    @Around("aspect()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        //获取返回结果
        Result result = (Result) point.proceed();
        //修改token - 只在需要時添加token
        if (result != null && treadLocalToken.get() != null){
            Object data = result.getData();
            if (data instanceof Map) {
                Map mapData = (Map) data;
                // 只有在原本沒有token時才添加
                if (!mapData.containsKey("token")) {
                    mapData.put("token", treadLocalToken.get());
                    result.setData(mapData);
                }
            } else if (data == null) {
                // 如果 data 為 null，創建一個新的 Map
                Map<String, Object> newData = new HashMap<>();
                newData.put("token", treadLocalToken.get());
                result.setData(newData);
            }
        }
        return result;
    }
}
