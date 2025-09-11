package com.example.emos.wx.task;

import com.example.emos.wx.exception.EmoException;
import com.example.emos.wx.pojo.MessageEntity;
import com.example.emos.wx.pojo.MessageRefEntity;
import com.example.emos.wx.service.MessageService;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@Slf4j
@Component
public class MessageTask {
    @Autowired
    private ConnectionFactory factory;

    @Autowired
    private MessageService messageService;

    public void send(String topic, MessageEntity entity){
        String id = messageService.insertMessage(entity);
        //创建和RabbitMQ的连接以及通道
        try(Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();)
        {
            // 声明一个队列，如果队列不存在则创建
            // 参数说明：队列名称、是否持久化、是否独占、是否自动删除、其他参数
            channel.queueDeclare(topic,true,false,false,null);
            // 创建一个HashMap用于存储消息头信息
            HashMap map = new HashMap();
            //将消息id放进消息头中，便于消费者识别和处理消息
            map.put("messageId",id);
            // 构建消息属性，设置消息头信息
            // 使用AMQP.BasicProperties.Builder模式构建属性对象
            AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().headers(map).build();
            // 发布消息到RabbitMQ
            // 参数说明：交换机名称(空字符串表示使用默认交换机)、路由键(队列名称)、消息属性,消息体
            channel.basicPublish("",topic,properties,entity.getMsg().getBytes(StandardCharsets.UTF_8));
            log.debug("消息发送成功：{}", entity.getMsg());

        }catch (Exception e){
            log.error("执行异常",e);
            throw new EmoException("向RabbitMQ发送消息异常");

        }
    }

    @Async
    public void sendAsync(String topic, MessageEntity entity){
        send(topic,entity);

    }

    public int receive(String topic){
        int i=0;
        try(Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();)
        {
            channel.queueDeclare(topic,true,false,false,null);
            while (true){
                GetResponse response = channel.basicGet(topic, false);
                if (response != null){
                    String messageId = response.getProps().getHeaders().get("messageId").toString();
                    byte[] body = response.getBody();
                    String message = new String(body,StandardCharsets.UTF_8);
                    log.debug("接收到消息：{}",message);

                    // 处理消息，存入mongo DB
                    MessageRefEntity messageRef = new MessageRefEntity();
                    messageRef.setMessageId(messageId);
                    // 将字符串形式的userId转换为Integer类型并存储
                    try {
                        Integer userId = Integer.parseInt(topic);
                        messageRef.setReceiverId(userId); // 直接存储Integer类型
                    } catch(NumberFormatException e) {
                        log.error("转换userId失败", e);
                        // 无法转换时尝试存储0（默认值）
                        messageRef.setReceiverId(0);
                    }
                    messageRef.setReadFlag(false);
                    messageRef.setLastFlag(true);
                    messageService.insertRef(messageRef);

                    channel.basicAck(response.getEnvelope().getDeliveryTag(),false);
                    i++;

                }else {
                    break;
                }

            }
        }catch (Exception e){
            log.error("执行异常",e);
            throw new EmoException("接收消息异常");

        }
        return i;
    }

    @Async
    public void receiveAsync(String topic){
        receive(topic);
    }

    public void deleteQueue(String topic){
        try(Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();)
        {
            channel.queueDelete(topic);
            log.debug("删除队列成功：{}",topic);
        }catch (Exception e){
            log.error("删除队列失败",e);
            throw new EmoException("删除队列失败");

        }
    }
    @Async
    public void deleteQueueAsync(String topic){
        deleteQueue(topic);
    }

}
