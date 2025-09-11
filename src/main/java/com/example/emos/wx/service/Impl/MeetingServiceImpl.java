package com.example.emos.wx.service.Impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.emos.wx.exception.EmoException;
import com.example.emos.wx.mapper.TbDeptMapper;
import com.example.emos.wx.mapper.TbMeetingMapper;
import com.example.emos.wx.mapper.TbUserMapper;
import com.example.emos.wx.pojo.TbMeeting;
import com.example.emos.wx.service.MeetingService;
import com.example.emos.wx.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
@Slf4j
public class MeetingServiceImpl implements MeetingService {

    @Autowired
    private TbMeetingMapper tbMeetingMapper;
    @Autowired
    private TbDeptMapper tbDeptMapper;
    @Autowired
    private TbUserMapper tbUserMapper;

    @Value("${emos.recieveNotify}")
    private String recievceNotify;

    @Value("${emos.workflow.url}")
    private String workflow;
    @Override
    public void insertMeeting(TbMeeting meeting) {
        int row = tbMeetingMapper.insertMeeting(meeting);
        if(row!=1){
            log.error("会议插入失败");
            throw new EmoException("会议添加失败");
        }
        //开启审批工作流
        startMeetingWorkFlow(meeting);

    }

    @Override
    public ArrayList<HashMap> searchMyMeetingListByPage(HashMap param) {
        ArrayList<HashMap> meetingList = tbMeetingMapper.searchMyMeetingListByPage(param);
        String date = null;
        HashMap resultMap = null ;
        JSONArray array= null;
        ArrayList resultList = new ArrayList<>();
        for (HashMap map : meetingList) {
            Object dateObj = map.get("date");
            if (dateObj == null) {
                continue; // 跳过date为null的记录
            }
            String temp = dateObj.toString();
            if (!temp.equals(date)){
                array = new JSONArray();
                date = temp;
                resultMap = new HashMap();
                resultMap.put("date",date);

                resultMap.put("list",array);
                resultList.add(resultMap);
            }
            array.put(map);
        }
        return resultList;
    }

    @Override
    public void startMeetingWorkFlow(TbMeeting meeting) {
        String uuid = meeting.getUuid();
        int creatorId = meeting.getCreatorId().intValue();
        String date = meeting.getDate();
        String start = meeting.getStart();
        
        HashMap userInfo = tbUserMapper.searchUserInfo(creatorId);
        log.info("查询用户信息，用户ID: {}, 结果: {}", creatorId, userInfo);
        
        if (userInfo == null || userInfo.isEmpty()) {
            log.error("未找到用户信息，用户ID: {}", creatorId);
            throw new EmoException("用户信息不存在");
        }
        
        JSONObject json = new JSONObject();
        json.set("uuid",uuid);
        json.set("creatorId",creatorId);
        // 优先使用name，如果没有则使用nickname
        String creatorName = userInfo.get("name") != null ? userInfo.get("name").toString() : 
                            (userInfo.get("nickname") != null ? userInfo.get("nickname").toString() : "未知用户");
        json.set("creatorName", creatorName); // 添加创建者姓名
        json.set("title", meeting.getTitle()); // 添加会议标题
        json.set("openId", userInfo.get("openId"));
        json.set("date",date);
        json.set("start", start.length() == 5 ? start + ":00" : start); // 确保时间格式为HH:mm:ss
        json.set("url", "http://192.168.1.6:8080/emos-wx-api/meeting/recieveNotify"); // 添加回调URL
        json.set("code", "MEETING"); // 业务类型标识（接口验证用）
        json.set("meetingType", meeting.getType() == 1 ? "线上会议" : "线下会议"); // 添加会议类型
        json.set("tcode", String.format("%06d", (int)(Math.random() * 1000000))); // 生成随机6位数字验证码
        
        Object roleObj = userInfo.get("role");
        if (roleObj == null) {
            log.error("用户角色信息为空，用户ID: {}, 用户信息: {}", creatorId, userInfo);
            throw new EmoException("用户角色信息异常");
        }
        log.info("用户角色信息: {}", roleObj.toString());
        String[] roles = roleObj.toString().split(",");
        if (!ArrayUtil.contains(roles,"总经理")){
                Integer managerId = tbUserMapper.searchDeptManagerId(creatorId);
                json.set("managerId",managerId);
            int gmId = tbUserMapper.searchGmId();
            json.set("gmId",gmId);
            Boolean bool = tbMeetingMapper.searchMeetingMembersInSameDept(uuid);
            json.set("sameDept",bool);
        }
        String url = workflow + "/workflow/startMeetingProcess";
        log.info("准备调用workflow服务，URL: {}", url);
        log.info("发送的JSON数据: {}", json.toString());
        HttpResponse resp = HttpRequest.post(url).header("Content-Type", "application/json")
                .body(json.toString()).execute();
        log.info("workflow服务响应状态码: {}", resp.getStatus());
        log.info("workflow服务响应内容: {}", resp.body());
        if(resp.getStatus()==200){
            json= JSONUtil.parseObj(resp.body());
            String instanceId  = json.getStr("instanceId");
            HashMap param = new HashMap();
            param.put("uuid",uuid);
            param.put("instanceId",instanceId);
            int row= tbMeetingMapper.updateMeetingInstanceId(param);
            if(row!=1){
                log.error("会议插入工作流实例ID失败");
                throw new EmoException("会议添加失败");
            }

        }

    }

    @Override
    public HashMap searchMeetingById(int id) {
        HashMap map = tbMeetingMapper.searchMeetingById(id);
        ArrayList<HashMap> list = tbMeetingMapper.searchMeetingMembers(id);
        map.put("members",list);
        return map;
    }

    @Override
    public void updateMeetingInfo(HashMap param) {
        int id = (int) param.get("id");
        String date = param.get("date").toString();
        String start = param.get("start").toString();
        String instanceId = param.get("instanceId").toString();

        //查找修改前的会议信息
        HashMap oldMeeting = tbMeetingMapper.searchMeetingById(id);
        if (oldMeeting == null || oldMeeting.isEmpty()) {
            log.error("未找到会议信息，会议ID: {}", id);
            throw new EmoException("会议不存在");
        }
        log.info("修改前会议信息: {}", oldMeeting);
        
        String uuid = oldMeeting.get("uuid").toString();
        Integer creatorId = Integer.parseInt(oldMeeting.get("creatorId").toString());
        Integer currentStatus = Integer.parseInt(oldMeeting.get("status").toString());
        log.info("准备更新会议，ID: {}, 当前状态: {}, 参数: {}", id, currentStatus, param);
        
        int row = tbMeetingMapper.updateMeetingInfo(param);
        log.info("数据库更新结果，影响行数: {}", row);
        if (row != 1){
            log.error("会议修改失败，影响行数: {}，会议ID: {}，当前状态: {}", row, id, currentStatus);
            throw new EmoException("会议修改失败，可能会议状态不允许修改或会议不存在");
        }
        JSONObject json = new JSONObject();
        json.set("instanceId",instanceId);
        json.set("reason","修改会议");
        json.set("uuid",uuid);
        json.set("code","MEETING");  // 修改为正确的code值
        json.set("type","会议");  // 添加必需的type字段
        json.set("tcode", String.format("%06d", (int)(Math.random() * 1000000))); // 添加必需的6位数字验证码
        String url = workflow + "/workflow/deleteProcessById";
        HttpResponse resp = HttpRequest.post(url).header("Content-Type", "application/json").body(json.toString()).execute();
        if (resp.getStatus() != 200){
            log.error("删除工作流失败");
            throw new EmoException("删除工作流失败");
        }

        //创建新的工作流
        // 构造更新后的会议对象
        TbMeeting updatedMeeting = new TbMeeting();
        updatedMeeting.setUuid(uuid);
        updatedMeeting.setCreatorId((long) creatorId);
        updatedMeeting.setTitle(param.get("title").toString());
        updatedMeeting.setDate(date);
        updatedMeeting.setStart(start);
        updatedMeeting.setEnd(param.get("end").toString());
        updatedMeeting.setType((Integer) param.get("type"));
        
        // 启动新的会议工作流
        startMeetingWorkFlow(updatedMeeting);

    }

    @Override
    public int deleteMeetingById(int id) {
        HashMap meeting = tbMeetingMapper.searchMeetingById(id);
        String uuid = meeting.get("uuid").toString();
        String instanceId = meeting.get("instanceId").toString();
        DateTime date = DateUtil.parse(meeting.get("date")+" " + meeting.get("start"));
        DateTime now = DateUtil.date();
        
        // 如果会议已经结束，允许删除
        if (now.isAfter(date)) {
            log.info("会议已过期，允许删除 - 会议ID: {}, 会议开始时间: {}, 当前时间: {}", 
                    id, date.toString(), now.toString());
            // 继续执行删除操作
        } else {
            // 只有未开始的会议才检查20分钟限制
            DateTime deleteDeadline = DateUtil.offset(date, DateField.MINUTE, -20);
            
            log.info("删除会议时间检查 - 会议ID: {}, 会议开始时间: {}, 当前时间: {}, 删除截止时间: {}", 
                    id, date.toString(), now.toString(), deleteDeadline.toString());
            
            if(now.isAfterOrEquals(deleteDeadline)){
                log.warn("会议删除被拒绝 - 距离开始时间不足20分钟");
                throw new EmoException("距离会议开始不足20分钟，不能删除");
            }
        }
        int row = tbMeetingMapper.deleteMeetingById(id);
        if(row!=1){
            throw new EmoException("会议删除失败");
        }
        //删除工作流

        JSONObject json = new JSONObject();
        json.set("instanceId",instanceId);
        json.set("reason","修改会议");
        json.set("uuid",uuid);
        json.set("code","MEETING");  // 修改为正确的code值
        json.set("type","会议");  // 添加必需的type字段
        json.set("tcode", String.format("%06d", (int)(Math.random() * 1000000))); // 添加必需的6位数字验证码
        String url = workflow + "/workflow/deleteProcessById";
        HttpResponse resp = HttpRequest.post(url).header("Content-Type", "application/json").body(json.toString()).execute();
        if (resp.getStatus() != 200){
            log.error("删除工作流失败");
            throw new EmoException("删除工作流失败");
        }
        return row;

    }


}
