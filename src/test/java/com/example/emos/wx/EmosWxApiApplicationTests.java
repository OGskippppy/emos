package com.example.emos.wx;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.example.emos.wx.pojo.MessageEntity;
import com.example.emos.wx.pojo.MessageRefEntity;
import com.example.emos.wx.pojo.TbMeeting;
import com.example.emos.wx.service.MeetingService;
import com.example.emos.wx.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
class EmosWxApiApplicationTests {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MeetingService meetingService;

    @Test
    void contextLoads() {

        for (int i = 0; i < 100; i++) {
            MessageEntity message = new MessageEntity();
            message.setUuid(IdUtil.simpleUUID());
            message.setSenderId(0);
            message.setSenderName("系统消息");
            message.setMsg(("这是第"+i+"条消息"));
            message.setSendTime(new Date());
            String id = messageService.insertMessage(message);


            MessageRefEntity ref = new MessageRefEntity();
            ref.setMessageId(id);
            ref.setReceiverId(19);
            ref.setReadFlag(false);
            ref.setLastFlag(true);
            messageService.insertRef(ref);
        }
    }

    @Test
    void addMeeting(){
        for (int i = 1; i < 100; i++) {
            TbMeeting meeting = new TbMeeting();
            meeting.setId(Long.valueOf(i));
            meeting.setUuid(IdUtil.simpleUUID());
            meeting.setTitle("第"+i+"个测试会议");
            meeting.setCreatorId(19L);
            meeting.setDate(DateUtil.today());
            meeting.setPlace("线上会议室");
            meeting.setStart("09:00");
            meeting.setEnd("10:00");
            meeting.setType(1);
            meeting.setMembers("[15,16]");
            meeting.setDesc("会议研讨Emos项目上线测试");
            meeting.setInstanceId(IdUtil.simpleUUID());
            meeting.setStatus(3);

            meetingService.insertMeeting(meeting);

        }


    }



}
