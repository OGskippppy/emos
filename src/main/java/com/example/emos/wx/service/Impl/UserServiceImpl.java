package com.example.emos.wx.service.Impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.emos.wx.exception.EmoException;
import com.example.emos.wx.mapper.TbDeptMapper;
import com.example.emos.wx.mapper.TbUserMapper;
import com.example.emos.wx.pojo.MessageEntity;
import com.example.emos.wx.pojo.TbUser;
import com.example.emos.wx.service.UserService;
import com.example.emos.wx.task.MessageTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.util.*;
import java.math.BigInteger;

@Slf4j
@Service
@Scope("prototype")
public class UserServiceImpl  extends ServiceImpl<TbUserMapper, TbUser> implements UserService {

    @Value("${wx.appid}")
    private String appid;

    @Value("${wx.appsecret}")
    private String appsecret;

    @Autowired
    private TbUserMapper tbUserMapper;

    @Autowired
    private MessageTask messageTask;

    @Autowired
    private TbDeptMapper tbDeptMapper;

    public String getOpenId(String code) {
        Map<String, Object> map = new HashMap<>();
        map.put("appid", appid);
        map.put("secret", appsecret);
        map.put("js_code",code  );
        map.put("grant_type","authorization_code");
        String postResult = HttpUtil.post("https://api.weixin.qq.com/sns/jscode2session", map);
        JSONObject jsonObject = JSONUtil.parseObj(postResult);
        String openid = jsonObject.getStr("openid");
        if (openid == null){
            throw new RuntimeException("临时凭证登陆错误");
        }


        return openid;
    }

    @Override
    public Set<String> searchUserPermissions(int userId) {
        try {
            Set<String> permissions = tbUserMapper.searchUserPermissions(userId);
            log.info("用戶權限查詢成功: userId={}, permissions={}", userId, permissions);
            return permissions;
        } catch (Exception e) {
            log.error("查詢用戶權限失敗: userId={}", userId, e);
            throw e;
        }
    }


    /**
     * 注册用户
     * @param registerCode
     * @param code
     * @param nickname
     * @param photo
     * @return
     */
    public Integer registerUser(String registerCode,String code,String nickname,String photo){
        log.info("收到註冊請求，微信授權碼: {}", code);
        //如果注册的是超级管理员
        if (registerCode.equals("000000")){
            Boolean bool = tbUserMapper.selectRootCount();
            //如果没有超级管理员
            if(!bool){
                String openId = getOpenId(code);
                Map<String,Object> param = new HashMap<>();
                param.put("openId",openId);
                param.put("nickname",nickname);
                param.put("photo",photo);
                param.put("role","[1]");
                param.put("root",1);
                param.put("deptId",1);
                param.put("name",nickname);
                param.put("status",1);
                param.put("createTime",new Date());
                // 设置入职日期为注册当天
                param.put("hiredate", DateUtil.today());
                tbUserMapper.insert(param);
                Object idObj = param.get("id");
                Integer id = null;
                if (idObj instanceof BigInteger) {
                    id = ((BigInteger) idObj).intValue();
                } else if (idObj instanceof Integer) {
                    id = (Integer) idObj;
                } else if (idObj instanceof Long) {
                    id = ((Long) idObj).intValue();
                }

                MessageEntity message = new MessageEntity();
                message.setSenderId(0);
                message.setSenderName("系统消息");
                message.setMsg("欢迎您注册Emos系统超级管理员，请及时完善个人资料");
                message.setSendTime(new Date());
                message.setUuid(IdUtil.simpleUUID());
                messageTask.sendAsync(id+"",message);

                if (id == null) {
                    throw new EmoException("用戶創建失敗");
                }
                log.info("用戶創建成功，用戶ID: {}", id);
                return id;
            }else {
                throw new EmoException("无法绑定超级管理员");
            }
        }
        //如果不是超级管理员，暂时抛出异常，因为还没有实现普通用户注册逻辑
        // throw new EmoException("该注册码无效，请联系管理员获取正确的注册码");

        // 测试注册码：注册为部门经理+总经理
        if (registerCode.equals("123456")) {
            String openId = getOpenId(code);
            Map<String, Object> param = new HashMap<>();
            param.put("openId", openId);
            param.put("nickname", nickname);
            param.put("photo", photo);
            param.put("role", "[2,3]");
            param.put("root", 0);
            param.put("deptName", "管理部");
            param.put("name", "李海城");
            param.put("status", 1);
            param.put("createTime", new Date());
            param.put("hiredate", DateUtil.today());
            tbUserMapper.insert(param);
            Object idObj = param.get("id");
            Integer id = null;
            if (idObj instanceof BigInteger) {
                id = ((BigInteger) idObj).intValue();
            } else if (idObj instanceof Integer) {
                id = (Integer) idObj;
            } else if (idObj instanceof Long) {
                id = ((Long) idObj).intValue();
            }

            if (id == null) {
                throw new EmoException("用户创建失败");
            }
            MessageEntity message = new MessageEntity();
            message.setSenderId(0);
            message.setSenderName("系统消息");
            message.setMsg("欢迎您注册Emos系统超级管理员，请及时完善个人资料");
            message.setSendTime(new Date());
            message.setUuid(IdUtil.simpleUUID());
            messageTask.sendAsync(id+"",message);
            log.info("测试用户创建成功，用户ID: {}, 用户名: OGskipppy, 角色: [2,3]", id);
            return id;
        }

        throw new EmoException("该注册码无效，请联系管理员获取正确的注册码");
    }

    public Integer login(String code){
        String openId = getOpenId(code);
        Integer id = tbUserMapper.searchIdByOpenId(openId);
        if (id == null){
            throw new EmoException("用户不存在");
        }
        messageTask.receiveAsync(id+"");
        return id;

    }

    @Override
    public TbUser searchById(Integer userId) {
        TbUser user = tbUserMapper.searchById(userId);
        return user;
    }

    @Override
    public String searchUserHiredate(int userId) {
        String hiredate = tbUserMapper.searchUserHiredate(userId);
        return hiredate;
    }

    @Override
    public HashMap searchUserSummary(int userId) {
        HashMap map = tbUserMapper.searchUserSummary(userId);
        return map;
    }
    @Override
    public ArrayList<HashMap> searchUserGroupByDept(String keyword) {
        ArrayList<HashMap> list_dept = tbDeptMapper.searchDeptMembers(keyword);
        ArrayList<HashMap> list_user = tbUserMapper.searchUserGroupByDept(keyword);
        for (HashMap map1 : list_dept) {
            ArrayList members = new ArrayList();
            long deptId = (long)map1.get("id");
            for (HashMap map2 : list_user) {
                long userDeptId = (long)map2.get("deptId");
                if (deptId==userDeptId){
                    members.add(map2);
                }
            }
            map1.put("members",members);
        }
        return list_dept;
    }

    @Override
    public ArrayList<HashMap> searchMembers(ArrayList params) {
        ArrayList<HashMap> list = tbUserMapper.searchMembers(params);
        return list;
    }

    @Override
    public ArrayList<HashMap> selectUserPhotoAndName(ArrayList<Integer> param) {
        ArrayList<HashMap> list = tbUserMapper.selectUserPhotoAndName(param);
        return list;
    }

    @Override
    public List<String> searchUserRoleNames(int userId) {
        return tbUserMapper.searchUserRoleNames(userId);
    }
}
