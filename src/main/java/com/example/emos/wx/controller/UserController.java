package com.example.emos.wx.controller;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.form.RegisterForm;
import com.example.emos.wx.controller.form.LoginForm;
import com.example.emos.wx.controller.form.SearchMembersForm;
import com.example.emos.wx.controller.form.SearchUserGroupByDeptForm;
import com.example.emos.wx.exception.EmoException;
import com.example.emos.wx.pojo.Result;
import com.example.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
@Api(tags = "用户模块web接口")
public class UserController {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;

    @ApiOperation("注册新用户")
    @PostMapping("/register")
    public Result registerUser(@Valid @RequestBody RegisterForm form) {
        try {
            Integer id = userService.registerUser(form.getRegisterCode(), form.getCode(), form.getNickname(), form.getPhoto());
            log.info("用戶註冊成功，用戶ID: {}", id);
            
            String token = jwtUtil.createToken(id);
            log.info("Token生成成功: {}", token);
            
            Set<String> permissions = userService.searchUserPermissions(id);
            log.info("權限查詢成功: {}", permissions);
            
            redisTemplate.opsForValue().set("token:" + token, id + "", cacheExpire, TimeUnit.DAYS);
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("permission", permissions);
            return Result.success(data);
        } catch (Exception e) {
            log.error("註冊過程中發生異常", e);
            throw e;
        }
    }

    @ApiOperation("登录系统")
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginForm form) {
        log.info("收到登錄請求，微信授權碼: {}", form.getCode());

        Integer id = userService.login(form.getCode());
        log.info("用戶登錄成功，用戶ID: {}", id);
        
        String token = jwtUtil.createToken(id);
        log.info("Token生成成功: {}", token);
        
        Set<String> permissions = userService.searchUserPermissions(id);
        log.info("權限查詢成功: {}", permissions);
        redisTemplate.opsForValue().set("token:" + token, id + "", cacheExpire, TimeUnit.DAYS);

        List<String> roleNames = userService.searchUserRoleNames(id);
        log.info("用戶角色查詢成功: userId={}, roles={}", id, roleNames);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("permission", permissions);
        data.put("role", roleNames);

        log.info("登錄響應數據: {}", data);
        return Result.success(data);
    }

    @ApiOperation("查询用户摘要信息")
    @GetMapping("/searchUserSummary")
    public Result searchUserSummary(@RequestHeader("token") String token ) {
        int userId = jwtUtil.getUserId(token);
        HashMap map = userService.searchUserSummary(userId);
        return Result.success(map);
    }

    @PostMapping("/searchUserGroupByDept")
    @ApiOperation("查询部门下的员工")
    @RequiresPermissions(value = {"ROOT", "EMPLOYEE:SELECT"}, logical = Logical.OR)
    public Result searchUserGroupByDept(@Valid @RequestBody SearchUserGroupByDeptForm form) {
        ArrayList<HashMap> list = userService.searchUserGroupByDept(form.getKeyword());
        return Result.success(list);
    }
    @PostMapping("/searchMembers")
    @ApiOperation("查询指定用户的成员")
    @RequiresPermissions(value = {"ROOT", "EMPLOYEE:SELECT"}, logical = Logical.OR)
    public Result searchMembers(@Valid @RequestBody SearchMembersForm form) {
        if(!JSONUtil.isJsonArray(form.getMembers())){
            throw new EmoException("members參數格式不正確");
        }
        List param = JSONUtil.parseArray(form.getMembers()).toList(Integer.class);
        ArrayList<HashMap> list = userService.searchMembers((ArrayList) param);
        return Result.success(list);

    }
}
