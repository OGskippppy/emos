package com.example.emos.wx.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "message")
public class MessageEntity implements Serializable {
    @Id
    private String _id;

    @Indexed(unique = true)
    private String uuid;

    @Indexed
    private int senderId;

    private String senderPhoto="https://emos-wx-api.oss-cn-beijing.aliyuncs.com/header/6910b6f2gy1hrshta5h4uj20b40b4glm.jpg";

    private String senderName;

    private String msg;

    @Indexed
    private Date sendTime;
}
