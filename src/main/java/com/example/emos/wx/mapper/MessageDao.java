package com.example.emos.wx.mapper;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import com.example.emos.wx.pojo.MessageEntity;
import com.example.emos.wx.pojo.MessageRefEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Repository
public class MessageDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    public String insert(MessageEntity message) {
        Date sendTime = message.getSendTime();
        sendTime = DateUtil.offset(sendTime, DateField.HOUR, 8);
        message.setSendTime(sendTime);
        MessageEntity messageEntity = mongoTemplate.insert(message);
        return messageEntity.get_id();
    }

    public HashMap searchMessageById(String id) {
        HashMap map = mongoTemplate.findById(id, HashMap.class, "message");
        
        if (map != null) {
            // 处理字段名映射：将Msg转换为msg
            if (map.containsKey("Msg")) {
                map.put("msg", map.get("Msg"));
                map.remove("Msg");
            }
            
            Date sendTime = (Date) map.get("sendTime");
            sendTime = DateUtil.offset(sendTime, DateField.HOUR, 8);
            map.replace("sendTime", DateUtil.format(sendTime, "yyyy/MM/dd HH:mm"));
        }
        
        return map;
    }

    public List<HashMap> searchMessageByPage(int userId, long start, int length) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.addFields()
                        .addField("id")
                        .withValue(new org.bson.Document("$toString", "$_id"))
                        .build(),
                Aggregation.lookup("message_ref", "id", "messageId", "ref"),
                // 同时匹配多种类型的userId (Integer, Double, String)
                Aggregation.match(
                    new Criteria().orOperator(
                        Criteria.where("ref.receiverId").is(userId),         // Integer类型
                        Criteria.where("ref.receiverId").is(Double.valueOf(userId)), // Double类型
                        Criteria.where("ref.receiverId").is(String.valueOf(userId))  // String类型
                    )
                ),
                // 映射所有需要的字段，注意MongoDB中字段名是大写的Msg
                Aggregation.project()
                    .andInclude("id", "senderId", "senderName", "senderPhoto", "sendTime", "ref", "msg") ,// 使用已创建的字符串id
                    // 将大写的Msg字段映射为小写的msg
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "sendTime")),
                Aggregation.skip(start),
                Aggregation.limit(length)
        );

        List<HashMap> list = mongoTemplate.aggregate(aggregation, "message", HashMap.class).getMappedResults();
        list.forEach(one -> {
            List<Object> ref = (List<Object>)one.get("ref");
            if (ref != null && !ref.isEmpty()) {
                // 处理可能的类型转换问题
                try {
                    MessageRefEntity messageRefEntity;
                    if (ref.get(0) instanceof MessageRefEntity) {
                        messageRefEntity = (MessageRefEntity)ref.get(0);
                    } else {
                        // 如果不是正确的类型，尝试手动转换
                        HashMap refMap = (HashMap)ref.get(0);
                        messageRefEntity = new MessageRefEntity();
                        messageRefEntity.set_id(refMap.get("_id").toString());
                        messageRefEntity.setReadFlag((Boolean)refMap.get("readFlag"));
                    }
                    Boolean readFlag = messageRefEntity.getReadFlag();
                    String refId = messageRefEntity.get_id();
                    one.put("readFlag", readFlag);
                    one.put("refId", refId);
                } catch (Exception e) {
                    // 出现异常时设置默认值
                    one.put("readFlag", false);
                    one.put("refId", "");
                    System.err.println("消息转换异常：" + e.getMessage());
                }
            }
            one.remove("ref");
            one.remove("_id");
            Date sendTime = (Date) one.get("sendTime");
            sendTime = DateUtil.offset(sendTime, DateField.HOUR, -8);
            one.put("sendTime", sendTime);

            String today = DateUtil.today();
            if(today.equals(DateUtil.date(sendTime).toDateStr())){
                one.put("sendTime",DateUtil.format(sendTime, "HH:mm"));
            }
            else {
                one.put("sendTime",DateUtil.format(sendTime, "yyyy/MM/dd"));
            }

        });
        return list;
    }
}
