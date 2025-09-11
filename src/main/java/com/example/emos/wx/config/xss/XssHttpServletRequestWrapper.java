package com.example.emos.wx.config.xss;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONUtil;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * 继承HttpServletRequestWrapper类
 * 添加过滤XSS跨站脚本攻击的逻辑
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (!StrUtil.hasEmpty( value)){
            value = HtmlUtil.filter(value);
        }
        return value;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        for (int i = 0; i < values.length; i++) {
            if (!StrUtil.hasEmpty( values[i])){
                values[i] = HtmlUtil.filter(values[i]);
            }
        }
        return values;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameters = super.getParameterMap();
        LinkedHashMap<String, String[]> map = new LinkedHashMap<>();
        for (String key : parameters.keySet()) {
            String[] values = parameters.get(key);
            for (int i = 0; i < values.length; i++) {
                if (!StrUtil.hasEmpty( values[i])){
                    values[i] = HtmlUtil.filter(values[i]);
                }
            }
            map.put(key, values);
        }
        return map;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        ServletInputStream in = super.getInputStream();
        InputStreamReader reader = new InputStreamReader(in, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder stringBuilder = new StringBuilder();
        String line = bufferedReader.readLine();
        while(!StrUtil.hasEmpty( line)){
            stringBuilder.append(line);
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        reader.close();
        in.close();
        Map<String, Object> map = JSONUtil.parseObj(stringBuilder.toString());
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : map.keySet()) {
            Object o = map.get(key);
            if(o instanceof String){
                result.put(key, HtmlUtil.filter(o.toString()));
            }else{
                result.put(key, o);
            }
        }
        String jsonStr = JSONUtil.toJsonStr(result);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(jsonStr.getBytes("UTF-8"));
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        };
    }
}


