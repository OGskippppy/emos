package com.example.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.constant.SystemConstant;
import com.example.emos.wx.controller.form.CheckinForm;
import com.example.emos.wx.controller.form.SearchMonthCheckinForm;
import com.example.emos.wx.exception.EmoException;
import com.example.emos.wx.mapper.TbCheckinMapper;
import com.example.emos.wx.pojo.Result;
import com.example.emos.wx.pojo.SearchMonthCheckinDTO;
import com.example.emos.wx.service.CheckinService;
import com.example.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.aggregation.DataTypeOperators;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/checkin")
@Slf4j
@Api(tags = "签到模块")
public class CheckinController {

    @Autowired
    private CheckinService checkinService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;
    @Autowired
    private SystemConstant constants;

    @Value("${emos.image-folder}")
    private String imageFolder;
    @Autowired
    private TbCheckinMapper tbCheckinMapper;


    @ApiOperation("签到")
    @GetMapping("/validCanCheckIn")
    public Result validCanCheckIn(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        String valid = checkinService.validCanCheckIn(userId, DateUtil.today());
        HashMap<String, String> map = new HashMap<>();
        map.put("msg", valid);
        return Result.success(map);
    }

    @ApiOperation("签到")
    @PostMapping("/checkin")
    public Result checkin(@RequestHeader("token") String token, 
                         @RequestParam("photo") MultipartFile file,
                         @RequestParam("address") String address,
                         @RequestParam("country") String country,
                         @RequestParam("province") String province,
                         @RequestParam("city") String city,
                         @RequestParam("district") String district) {
        Integer userId = jwtUtil.getUserId(token);
        String fileName = file.getOriginalFilename().toLowerCase();
        if (!fileName.endsWith(".jpg")){
            return Result.error("上传文件格式错误");
        }
        else {
            String path = imageFolder + "/" + fileName;
            try {
                file.transferTo(Paths.get( path));
                HashMap param = new HashMap();
                param.put("userId", userId);
                param.put("path", path);
                param.put("city", city);
                param.put("district", district);
                param.put("province", province);
                param.put("country", country);
                param.put("address", address);
                checkinService.checkin(param);
                return Result.success("签到成功");
            } catch (RuntimeException e) {
                if ("人脸模型不存在".equals(e.getMessage())) {
                    return Result.error("不存在人脸模型");
                }
                throw e;
            } catch (IOException e) {
                log.error("图片保存错误", e);
                throw new EmoException("图片保存错误");
            } finally {
                FileUtil.del(path);
            }

        }

    }


    @PostMapping("/createFaceModel")
    @ApiOperation("创建人脸模型")
    public Result createFaceModel(@RequestHeader("token") String token,@RequestParam("photo") MultipartFile file){
        Integer userId = jwtUtil.getUserId(token);
        String fileName = file.getOriginalFilename().toLowerCase();
        if (!fileName.endsWith(".jpg")){
            return Result.error("上传文件格式错误");
        }
        else {
            String path = imageFolder + "/" + fileName;
            try {
                file.transferTo(Paths.get( path));
                checkinService.createFaceModel(userId, path);
                return Result.success("人脸模型创建成功");
            } catch (IOException e) {
                log.error("图片保存错误", e);
                throw new EmoException("图片保存错误");
            }finally {
                FileUtil.del(path);
            }

        }

    }

    @GetMapping("/searchTodayCheckin")
    @ApiOperation("查询今日签到")
    public Result searchTodayCheckin(@RequestHeader("token") String token){
        int userId = jwtUtil.getUserId(token);
        HashMap map = checkinService.searchTodayCheckin(userId);
        if (map == null) {
            map = new HashMap();
        }
        map.put("attendanceTime",constants.attendanceTime);
        map.put("closingTime",constants.closingTime);
        long days = checkinService.searchCheckinDays(userId);
        map.put("checkinDays",days);

        DateTime hireDate = DateUtil.parse(userService.searchUserHiredate(userId));
        DateTime startDate = DateUtil.beginOfWeek(DateUtil.date());
        if (startDate.isBefore(hireDate)){
            startDate = hireDate;
        }
        DateTime endDate = DateUtil.endOfWeek(DateUtil.date());
        HashMap param = new HashMap();
        param.put("startDate",startDate.toString());
        param.put("endDate",endDate.toString());
        param.put("userId",userId);
        ArrayList<HashMap> list = checkinService.searchWeekCheckin(param);
        map.put("weekCheckin",list);
        
        // 添加最终诊断日志
        log.info("即将返回给前端的最终数据: {}", map);
        
        return Result.success(map);
    }

    @PostMapping("/searchMonthCheckin")
    @ApiOperation("查询月签到")
    public Result searchMonthCheckin(@RequestHeader("token") String token,@Valid@RequestBody SearchMonthCheckinForm form){
        log.info("=== searchMonthCheckin 收到请求 ===");
        log.info("form: year={}, month={}", form.getYear(), form.getMonth());

        int userId = jwtUtil.getUserId(token);
        log.info("userId: {}", userId);
        
        String hireDateStr = userService.searchUserHiredate(userId);
        DateTime hireDate;
        if (hireDateStr == null || hireDateStr.trim().isEmpty()) {
            // 如果入职日期为空，使用默认日期
            hireDate = DateUtil.parse("2020-01-01");
        } else {
            hireDate = DateUtil.parse(hireDateStr);
        }
            
        String month =form.getMonth()<10?"0"+form.getMonth(): String.valueOf(form.getMonth());
        DateTime startDate = DateUtil.parse(form.getYear()+"-"+month+"-01");
        
        if (startDate.isBefore(DateUtil.beginOfMonth(startDate))){
            throw new EmoException("开始日期不能早于当前日期");
        }
        if (startDate.isBefore(hireDate)){
            startDate = hireDate;
        }
        DateTime endDate = DateUtil.endOfMonth(startDate);
        
        int sum_1=0;
        int sum_2=0;
        int sum_3=0;
        HashMap param = new HashMap();
        param.put("startDate",startDate.toString());
        param.put("endDate",endDate.toString());
        param.put("userId",userId);

        ArrayList<HashMap> list = checkinService.searchMonthCheckin(param);

        log.info("=== searchMonthCheckin 诊断 ===");
        log.info("userId: {}, hireDate: {}", userId, hireDateStr);
        log.info("startDate: {}, endDate: {}", startDate, endDate);
        log.info("list 大小: {}, 内容: {}", list.size(), list);

        for (HashMap one : list) {
            String type = one.get("type").toString();
            String status = one.get("status").toString();
            log.info("日期: {}, type: {}, status: {}", one.get("date"), type, status);
            if (type.equals("工作日")){
                if (status.equals("正常")){
                    sum_1++;
                }
                else if (status.equals("迟到")){
                    sum_2++;
                }
                else if (status.equals("缺勤")){
                    sum_3++;
                }
            }
        }
        
        HashMap<Object,Object> map = new HashMap<>();
        map.put("list",list);
        map.put("sum_1",sum_1);
        map.put("sum_2",sum_2);
        map.put("sum_3",sum_3);
        log.info("最终返回给前端的 map: {}", map);
        return Result.success(map);

    }
}
