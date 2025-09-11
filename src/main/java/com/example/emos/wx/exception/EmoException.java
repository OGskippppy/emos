package com.example.emos.wx.exception;

import lombok.Data;

@Data
public class EmoException extends RuntimeException{
    private String msg;
    private int code = 500;
    public EmoException(String msg){
        super(msg);
        this.msg = msg;
    }
    public EmoException(String msg,int code){
        super(msg);
        this.msg = msg;
        this.code = code;
    }
    public EmoException(String msg,Throwable e){
        super(msg,e);
        this.msg = msg;
    }
    public EmoException(String msg,int code,Throwable e){
        super(msg,e);
        this.msg = msg;
        this.code = code;
    }
}
