package com.example.emos.wx.service;

import com.example.emos.wx.pojo.MessageEntity;
import com.example.emos.wx.pojo.MessageRefEntity;

import java.util.HashMap;
import java.util.List;

public interface MessageService {
    public String insertMessage(MessageEntity message);
    public HashMap searchMessageById(String id);
    public List<HashMap> searchMessageByPage(int userId, long start, int length);
    public String insertRef(MessageRefEntity messageRef);
    public long searchUnreadCount(int userId);
    public long searchLastCount(int userId);
    public long updateUnreadMessage(String id);
    public long deleteMessageRefById(String id);
    public long deleteUserMessageRef(int userId);
    
    // 添加获取MongoTemplate的方法
    public org.springframework.data.mongodb.core.MongoTemplate getMongoTemplate();
}
