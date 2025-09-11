package com.example.emos.wx.controller;

import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.form.DeleteMessageRefByIdForm;
import com.example.emos.wx.controller.form.SearchMessageByIdForm;
import com.example.emos.wx.controller.form.SearchMessageByPageForm;
import com.example.emos.wx.controller.form.UpdateUnreadMessageForm;
import com.example.emos.wx.pojo.Result;
import com.example.emos.wx.service.MessageService;
import com.example.emos.wx.task.MessageTask;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@Api(tags = "消息模块Web接口")
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageTask messageTask;

    @ApiOperation("查询用户消息")
    @PostMapping("/searchMessageByPage")
    public Result searchMessageByPage(@Valid@RequestBody SearchMessageByPageForm form, @RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        long page = form.getPage();
        int length = form.getLength();
        long start = (page - 1) * length;
        
        // 确保消息已同步
        messageTask.receiveAsync(userId + "");
        
        List<HashMap> list = messageService.searchMessageByPage(userId, start, length);
        return Result.success(list);
    }

    @PostMapping("/searchMessageById")
    @ApiOperation("根据id查询消息")
    public Result searchMessageById(@Valid@RequestBody SearchMessageByIdForm  form){
        HashMap map = messageService.searchMessageById(form.getId());
        return Result.success(map);
    }

    @PostMapping("/updateUnreadMessage")
    @ApiOperation("更新未读消息为已读")
    public Result updateUnreadMessage(@Valid@RequestBody UpdateUnreadMessageForm form){
        long rows = messageService.updateUnreadMessage(form.getId());
        return Result.success(rows==1?true:false);
    }

    @PostMapping("/deleteMessageRefById")
    @ApiOperation("删除消息")
    public Result deleteMessageRefById(@Valid@RequestBody DeleteMessageRefByIdForm form){
        long rows = messageService.deleteMessageRefById(form.getId());
        return Result.success(rows==1?true:false);
    }


    @GetMapping("/refreshMessage")
    @ApiOperation("刷新消息")
    public Result refreshMessage(@RequestHeader("token") String token){
        int userId = jwtUtil.getUserId(token);
        //异步接收消息
        messageTask.receiveAsync(userId+ "");
        //查询接受了多少条信息
        long lastRows = messageService.searchLastCount(userId);
        //查询未读信息
        long unreadRows = messageService.searchUnreadCount(userId);
        HashMap map = new HashMap();
        map.put("lastRows",lastRows);
        map.put("unreadRows",unreadRows);
        return Result.success(map);

    }
}
