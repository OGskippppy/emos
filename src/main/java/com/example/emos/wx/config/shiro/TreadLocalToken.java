package com.example.emos.wx.config.shiro;

import org.springframework.stereotype.Component;

@Component
public class TreadLocalToken {
    private static ThreadLocal threadLocal = new ThreadLocal();
    public static void set(String token)
    {
        threadLocal.set(token);
    }
    public static String get()
    {
        return (String) threadLocal.get();
    }
    public static void remove()
    {
        threadLocal.remove();
    }
}
