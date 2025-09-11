package com.example.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.form.*;
import com.example.emos.wx.exception.EmoException;
import com.example.emos.wx.pojo.Result;
import com.example.emos.wx.pojo.TbMeeting;
import com.example.emos.wx.service.MeetingService;
import com.example.emos.wx.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/meeting")
@Slf4j
public class MeetingController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private MeetingService meetingService;
    @Autowired
    private UserService userService;

    @PostMapping("/searchMyMeetingListByPage")
    public Result searchMyMeetingListByPage(@Valid@RequestBody SearchMyMeetingListByPageForm form, @RequestHeader("token") String token){
        int userId = jwtUtil.getUserId(token);
        HashMap param = new HashMap();
        int page = form.getPage();
        int length = form.getLength();
        long start = (page - 1) * length;
        param.put("userId", userId);
        param.put("start", start);
        param.put("length", length);
        ArrayList<HashMap> list = meetingService.searchMyMeetingListByPage(param);
        return Result.success(list);

    }

    @PostMapping("/insertMeeting")
    @ApiOperation("创建会议")
    @RequiresPermissions(value = {"ROOT","MEETING:INSERT"},logical = Logical.OR)
    public Result insertMeeting(@Valid @RequestBody InsertMeetingForm form, @RequestHeader("token") String token){
        if (form.getType()==2&&(form.getPlace()==null||form.getPlace().length()==0)){
            throw new EmoException("线下会议必须填写会议地点");
        }
        DateTime d1 = DateUtil.parse(form.getDate() + " " + form.getStart() + ":00");
        DateTime d2 = DateUtil.parse(form.getDate() + " " + form.getEnd() + ":00");
        if (d2.isBeforeOrEquals(d1)){
            throw new EmoException("结束时间必须大于开始时间");
        }
        if (!JSONUtil.isJsonArray(form.getMembers())){
            throw new EmoException("members不是JSON数组");
        }
        TbMeeting meeting = new TbMeeting();
        meeting.setUuid(UUID.randomUUID().toString());
        meeting.setTitle(form.getTitle());
        meeting.setCreatorId((long)jwtUtil.getUserId(token));
        meeting.setDate(form.getDate());
        meeting.setPlace(form.getPlace());
        meeting.setStart(form.getStart()+":00");
        meeting.setEnd(form.getEnd()+":00");
        meeting.setType(Integer.valueOf(form.getType()));
        meeting.setMembers(form.getMembers());
        meeting.setDesc(form.getDesc());
        meeting.setStatus(1);
        meetingService.insertMeeting(meeting);
        return Result.success();
    }


    @PostMapping("/searchMeetingById")
    @ApiOperation("根据ID查询会议")
    @RequiresPermissions(value = {"ROOT","MEETING:SELECT"},logical = Logical.OR)
    public Result searchMeetingById(@Valid @RequestBody SearchMeetingByIdForm form){
        HashMap map = meetingService.searchMeetingById(form.getId());
        return Result.success(map);
    }

    @PostMapping("/updateMeetingInfo")
    @ApiOperation("更新会议信息")
    @RequiresPermissions(value = {"ROOT","MEETING:UPDATE"},logical = Logical.OR)
    public Result updateMeetingInfo(@Valid @RequestBody UpdateMeetingInfoForm form){
        if (form.getType()==2&&(form.getPlace()==null||form.getPlace().length()==0)){
            throw new EmoException("线下会议必须填写会议地点");
        }
        DateTime d1 = DateUtil.parse(form.getDate() + " " + form.getStart() + ":00");
        DateTime d2 = DateUtil.parse(form.getDate() + " " + form.getEnd() + ":00");
        if (d2.isBeforeOrEquals(d1)){
            throw new EmoException("结束时间必须大于开始时间");
        }
        if (!JSONUtil.isJsonArray(form.getMembers())){
            throw new EmoException("members不是JSON数组");
        }
        HashMap param = new HashMap();
        param.put("title",form.getTitle());
        param.put("date",form.getDate());
        param.put("place",form.getPlace());
        param.put("start",form.getStart()+":00");
        param.put("end",form.getEnd()+":00");
        param.put("type",Integer.valueOf(form.getType()));
        param.put("members",form.getMembers());
        param.put("desc",form.getDesc());
        param.put("id",form.getId());
        param.put("instanceId",form.getInstanceId());
        param.put("status",1);
        meetingService.updateMeetingInfo(param);
        return Result.success();
    }

    @PostMapping("/deleteMeetingById")
    @ApiOperation("删除会议")
    @RequiresPermissions(value = {"ROOT","MEETING:DELETE"},logical = Logical.OR)
    public Result deleteMeetingById(@Valid @RequestBody DeleteMeetingByIdForm form){
        meetingService.deleteMeetingById(form.getId());
        return Result.success();
    }

    @PostMapping("/recieveNotify")
    @ApiOperation("接收工作流通知")
    public Result reciceveNotify(@Valid @RequestBody RecieveNotifyForm form){
        if(!form.getResult().equals("同意")){
            log.debug(form.getUuid()+"的会议审批通过");
        }
        else {
            log.debug(form.getUuid()+"的会议审批不通过");

        }
        return Result.success();
    }
    @PostMapping("/selectUserPhotoAndName")
    @ApiOperation("查询用户头像和名称")
    @RequiresPermissions(value = {"WORKFLOW:APPROVAL"})
    public Result selectUserPhotoAndName(@Valid @RequestBody SelectUserPhotoAndNameForm form){
        if(!JSONUtil.isJsonArray(form.getIds())){
            throw new EmoException("ids不是JSON数组");
        }
        ArrayList<Integer> param = (ArrayList<Integer>) JSONUtil.parseArray(form.getIds()).toList(Integer.class);
        ArrayList<HashMap> list = userService.selectUserPhotoAndName(param);
        return Result.success(list);
    }


}
