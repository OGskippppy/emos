package com.example.emos.wx.pojo;

import java.util.HashMap;

/**
 * 通用结果返回类
 */
public class Result  {
    private int code;        // 状态码
    private String msg;      // 消息
    private Object data;     // 数据

    // 构造方法
    public Result() {
    }

    public Result(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 成功的静态方法
    public static Result success() {
        return new Result(200, "success", null);
    }

    public static Result success(Object data) {
        return new Result(200, "success", data);
    }

    public static Result success(String msg, Object data) {
        return new Result(200, msg, data);
    }

    // 失败的静态方法
    public static Result error() {
        return new Result(500, "error", null);
    }

    public static Result error(String msg) {
        return new Result(500, msg, null);
    }

    public static Result error(int code, String msg) {
        return new Result(code, msg, null);
    }

    // getter和setter方法
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }

}
