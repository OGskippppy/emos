package com.example.emos.wx.service.Impl;


import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;
import com.example.emos.wx.constant.SystemConstant;
import com.example.emos.wx.exception.EmoException;
import com.example.emos.wx.mapper.*;
import com.example.emos.wx.pojo.*;
import com.example.emos.wx.service.CheckinService;
import com.example.emos.wx.task.MessageTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Service
@Scope("prototype")
@Slf4j
public class CheckinServiceImpl implements CheckinService {
    @Autowired
    private TbHolidaysMapper tbHolidaysMapper;

    @Autowired
    private TbWorkdayMapper tbWorkdayMapper;

    @Autowired
    private TbCheckinMapper tbCheckinMapper;

    @Autowired
    private SystemConstant systemConstant;

    @Autowired
    private TbFaceModelMapper faceModelMapper;

    @Value("${emos.face.code}")
    private String code;

    @Autowired
    private TbUserMapper TbUserMapper;

    @Value("${emos.face.createFaceModelUrl}")
    private String creatFaceModelUrl;

    @Value("${emos.face.checkinUrl}")
    private String checkinUrl;
    @Autowired
    private MessageTask messageTask;

    @Override
    public String validCanCheckIn(int userId, String date) {
        Integer holidayResult = tbHolidaysMapper.seachTodayIsHoliday();
        Integer workdayResult = tbWorkdayMapper.seachTodayIsWorkday();
        
        Boolean bool1 = holidayResult != null && holidayResult == 1;
        Boolean bool2 = workdayResult != null && workdayResult == 1;
        String type="工作日";
        if(DateUtil.date().isWeekend()){
            type="节假日";
        }
        if(bool1){
            type="节假日";
        } else if (bool2) {
            type="工作日";
        }

        if(type.equals("节假日")){
            return "节假日不需要签到";
        }else {
            DateTime now = DateUtil.date();
            String start =DateUtil.today() + " " + systemConstant.attendanceStartTime;
            String end = DateUtil.today() + " " +systemConstant.attendanceEndTime;
            DateTime attendanceStart = DateUtil.parse(start);
            DateTime attendanceEnd = DateUtil.parse(end);
            if (now.isBefore(attendanceStart)){
                return "今日未到签到时间";
            } else if (now.isAfter(attendanceEnd)) {
                return "今日已过签到时间";
            }else {
                HashMap map = new HashMap();
                map.put("userId",userId);
                map.put("date",date);
                map.put("start",attendanceStart);
                map.put("end",attendanceEnd);
                boolean bool = tbCheckinMapper.haveCheckin(map) != null ? true : false;
                return bool ? "今日已簽到" : "可以考勤";
            }
        }
    }

    @Override
    public void checkin(HashMap param) {
        log.info("開始簽到流程，參數: {}", param);
        
        // 嚴格時間窗口校驗：未到開始時間或已過截止時間直接拒絕
        Date now = DateUtil.date();
        log.info("當前時間: {}", now);
        
        String startTimeStr = DateUtil.today() + " " + systemConstant.attendanceStartTime;
        String normalDeadlineStr = DateUtil.today() + " " + systemConstant.attendanceTime;
        String endTimeStr = DateUtil.today() + " " + systemConstant.attendanceEndTime;
        
        log.info("考勤開始時間: {}", startTimeStr);
        log.info("正常截止時間: {}", normalDeadlineStr);
        log.info("考勤結束時間: {}", endTimeStr);
        
        Date start = DateUtil.parse(startTimeStr);
        Date normalDeadline = DateUtil.parse(normalDeadlineStr);
        Date end = DateUtil.parse(endTimeStr);
        
        if (now.before(start)) {
            log.error("今日未到签到时间: now={}, start={}", now, start);
            throw new EmoException("今日未到签到时间");
        }
        if (now.after(end)) {
            log.error("今日已过签到时间: now={}, end={}", now, end);
            throw new EmoException("今日已过签到时间");
        }
        
        // 狀態：正常(1) 截止到 attendanceTime；之後到 end 為遲到(2)
        int status = now.compareTo(normalDeadline) <= 0 ? 1 : 2;
        log.info("簽到狀態: {}", status);
        
        //判断该员工是否有人脸模型
        int userId = (Integer) param.get("userId");
        log.info("用戶ID: {}", userId);
        String faceModel = faceModelMapper.searchFaceModel(userId);
        log.info("人臉模型: {}", faceModel != null ? "存在" : "不存在");
        if (faceModel == null){
            log.error("人脸模型不存在");
            throw new RuntimeException("人脸模型不存在");
        }
        
        // 檢查今日是否已簽到
        HashMap checkParam = new HashMap();
        checkParam.put("userId", userId);
        checkParam.put("date", DateUtil.today());
        checkParam.put("start", start);
        checkParam.put("end", end);
        Integer existingCheckin = tbCheckinMapper.haveCheckin(checkParam);
        if (existingCheckin != null) {
            log.info("用戶今日已簽到，記錄ID: {}", existingCheckin);
            throw new EmoException("今日已簽到，請勿重複簽到");
        }
        

        
        // 驗證人臉模型格式
        if (faceModel.contains("error") || faceModel.contains("message") || faceModel.contains("status")) {
            log.error("人臉模型數據異常: {}", faceModel);
            throw new EmoException("人臉模型數據異常，請重新創建");
        }
        
        if (!faceModel.startsWith("[") || !faceModel.endsWith("]")) {
            log.error("人臉模型格式錯誤: {}", faceModel);
            throw new EmoException("人臉模型格式錯誤，請重新創建");
        }
        
        String path = param.get("path").toString();
        log.info("開始調用人臉識別服務，圖片路徑: {}", path);
        
        // 檢查文件是否存在
        File photoFile = new File(path);
        if (!photoFile.exists()) {
            log.error("簽到圖片文件不存在: {}", path);
            throw new EmoException("簽到圖片文件不存在: " + path);
        }
        
        log.info("簽到圖片文件大小: {} bytes", photoFile.length());
        log.info("簽到圖片文件是否可讀: {}", photoFile.canRead());
        
        HttpRequest request = HttpRequest.post(checkinUrl);
        log.info("人臉識別服務URL: {}", checkinUrl);
        log.info("驗證碼: {}", code);
        log.info("人臉模型長度: {}", faceModel.length());
        
        request.form("image", FileUtil.file(path),"targetModel",faceModel);
        request.form("code", code);
        
        log.info("開始發送簽到請求到人臉識別服務...");
        HttpResponse response = request.execute();
        
        log.info("人臉識別服務響應狀態: {}", response.getStatus());
        log.info("人臉識別服務響應內容: {}", response.body());
        
        if (response.getStatus() != 200){
            log.error("人脸识别失败，HTTP狀態: {}", response.getStatus());
            throw new EmoException("人脸识别失败");
        }

        String body = response.body();
        try {
            JSONObject obj = JSONUtil.parseObj(body);
            String msg = obj.getStr("message");
            String statusField = obj.getStr("status");

            // 判断 status 是否为 success，这是最稳妥的判断方式
            if ("success".equalsIgnoreCase(statusField)) {
                log.info("人臉識別成功，開始保存簽到記錄");

                try {
                    // 保存签到记录
                    String address = (String) param.get("address");
                    String country = (String) param.get("country");
                    String province = (String) param.get("province");
                    String city = (String) param.get("city");
                    String district = (String) param.get("district");

                    TbCheckin entity = new TbCheckin();
                    entity.setUserId(userId);
                    entity.setAddress(address);
                    entity.setCountry(country);
                    entity.setProvince(province);
                    entity.setCity(city);
                    entity.setDistrict(district);
                    entity.setStatus((int) status);
                    entity.setDate(DateUtil.today());
                    entity.setCreateTime(now);

                    log.info("準備插入簽到記錄: {}", entity);
                    int result = tbCheckinMapper.insertCheckin(entity);
                    log.info("簽到記錄插入結果: {}", result);
                    MessageEntity message = new MessageEntity();
                    message.setSenderId(0);
                    message.setSenderName("系统消息");
                    message.setMsg("你已完成签到");
                    message.setSendTime(new Date());
                    message.setUuid(IdUtil.simpleUUID());

                    messageTask.sendAsync(userId+"",message);

                    if (result <= 0) {
                        log.error("簽到記錄保存失敗");
                        throw new EmoException("簽到記錄保存失敗");
                    }
                } catch (org.springframework.dao.DuplicateKeyException e) {
                    log.error("重複簽到異常: {}", e.getMessage());
                    throw new EmoException("今日已簽到，請勿重複簽到");
                } catch (Exception dbException) {
                    // 专门捕获数据库或业务逻辑异常
                    log.error("保存签到记录时发生异常: ", dbException);
                    throw new EmoException("保存签到记录失败，系统内部错误");
                }
            } else {
                log.error("人臉識別失敗: {}", msg);
                throw new EmoException("签到失败: " + msg);
            }
        } catch (cn.hutool.json.JSONException e) {
            // 专门捕获 JSON 解析异常
            log.error("解析人臉識別響應失敗: {}, 異常: {}", body, e.getMessage());
            throw new EmoException("人臉識別服務返回格式異常");
        }
    }

    @Override
    public void createFaceModel(int userId, String path) {
        log.info("開始為用戶 {} 創建人臉模型", userId);
        log.info("圖片路徑: {}", path);
        
        // 檢查文件是否存在
        File photoFile = new File(path);
        if (!photoFile.exists()) {
            log.error("圖片文件不存在: {}", path);
            throw new EmoException("圖片文件不存在: " + path);
        }
        
        log.info("圖片文件大小: {} bytes", photoFile.length());
        log.info("圖片文件是否可讀: {}", photoFile.canRead());
        
        HttpRequest request = HttpRequest.post(creatFaceModelUrl);
        log.info("人臉識別服務URL: {}", creatFaceModelUrl);
        log.info("驗證碼: {}", code);
        
        // 修復：分別設置表單參數，確保圖片文件正確傳遞
        request.form("image", FileUtil.file(path));
        request.form("code", code);
        
        log.info("開始發送請求到人臉識別服務...");
        log.info("請求參數 - image: {}, code: {}", path, code);
        HttpResponse response = request.execute();
        String body = response.body();
        log.info("人臉識別服務返回: {}", body);
        
        // 以 JSON 方式嚴格解析
        try {
            JSONObject obj = JSONUtil.parseObj(body);
            String status = obj.getStr("status");
            if (!"success".equalsIgnoreCase(status)) {
                String msg = obj.getStr("message", "人臉模型創建失敗");
                log.error("人臉模型創建失敗: {}", body);
                throw new EmoException("人臉模型創建失敗: " + msg);
            }
            // 取出 data（為人臉特徵向量數組），轉回 JSON 字串保存
            Object data = obj.get("data");
            String modelJson = JSONUtil.toJsonStr(data);
            // 驗證格式為數值數組
            try {
                String[] numbers = modelJson.substring(1, modelJson.length() - 1).split(",");
                for (String num : numbers) {
                    Double.parseDouble(num.trim());
                }
            } catch (Exception e) {
                log.error("人臉模型數據格式無效: {}", modelJson);
                throw new EmoException("人臉模型數據格式無效，請重新拍照");
            }
            TbFaceModel entity = new TbFaceModel();
            entity.setUserId(userId);
            entity.setFaceModel(modelJson);
            faceModelMapper.insert(entity);
            log.info("用戶 {} 的人臉模型創建成功", userId);
            return;
        } catch (cn.hutool.json.JSONException ignore) {
            // 非 JSON 兼容舊格式（理論上不會出現）
            if (body.contains("error") || body.contains("message") || body.contains("status")) {
                log.error("人臉模型創建失敗: {}", body);
                throw new EmoException("人臉模型創建失敗: " + body);
            }
            if (!body.startsWith("[") || !body.endsWith("]")) {
                log.error("人臉模型格式錯誤: {}", body);
                throw new EmoException("人臉模型格式錯誤，請重新拍照");
            }
            try {
                String[] numbers = body.substring(1, body.length() - 1).split(",");
                for (String num : numbers) {
                    Double.parseDouble(num.trim());
                }
            } catch (Exception e) {
                log.error("人臉模型數據格式無效: {}", body);
                throw new EmoException("人臉模型數據格式無效，請重新拍照");
            }
            TbFaceModel entity = new TbFaceModel();
            entity.setUserId(userId);
            entity.setFaceModel(body);
            faceModelMapper.insert(entity);
            log.info("用戶 {} 的人臉模型創建成功(兼容舊格式)", userId);
        }
    }

    @Override
    public HashMap searchTodayCheckin(int userId) {
        // 步骤1：独立查询用户信息，返回强类型的DTO对象
        UserInfoDTO user_info_dto = tbCheckinMapper.searchUserInfo(userId);
        // 将DTO转换为HashMap
        HashMap user_info = user_info_dto != null ? new HashMap(cn.hutool.core.bean.BeanUtil.beanToMap(user_info_dto)) : new HashMap();

        // 步骤2：独立查询当天的签到记录
        HashMap checkin_info = tbCheckinMapper.searchTodayCheckin(userId);

        // 步骤3：如果签到信息不为空，则安全地合并
        if (checkin_info != null && !checkin_info.isEmpty()) {
            user_info.putAll(checkin_info);
        }

        return user_info;
    }

    @Override
    public long searchCheckinDays(int userId) {
        long l = tbCheckinMapper.searchCheckinDays(userId);
        return l;
    }

    @Override
    public ArrayList<HashMap> searchWeekCheckin(HashMap param) {
        ArrayList<HashMap> checkinList = tbCheckinMapper.searchWeekCheckin(param);
        log.info("=== searchWeekCheckin 诊断 ===");
        log.info("SQL查询结果 checkinList: {}", checkinList);
        ArrayList holidays = tbHolidaysMapper.searchHolidaysInRange(param);
        log.info("节假日列表 holidays: {}", holidays);
        ArrayList workday = tbWorkdayMapper.searchWorkdayInRange(param);
        log.info("工作日列表 workday: {}", workday);
        Date startDate = DateUtil.parse(param.get("startDate").toString());
        Date endDate = DateUtil.parse(param.get("endDate").toString());
        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);
        ArrayList<HashMap> list = new ArrayList();
        range.forEach(one -> {
            String date = DateUtil.format(one, "yyyy-MM-dd");
            String type = "工作日";
            if(one.isWeekend()){
                type = "节假日";
            }
            if (holidays!=null && holidays.contains(date)) {
                type = "节假日";
            } else if (workday !=null&& workday.contains(date)) {
                type = "工作日";
            }
            String status = "";
            Boolean flag = false;
            if (type.equals("工作日")&&DateUtil.compare(one, DateUtil.date())<=0){
                status = "缺勤";
                for (HashMap checkin : checkinList) {
                    if (checkin.containsValue( date)){
                        status = checkin.get("status").toString();
                        flag = true;
                        break;
                    }
                }
            }
            DateTime endTime = DateUtil.parse(DateUtil.today()+" "+systemConstant.attendanceStartTime);
            String today = DateUtil.today();
            if (date.equals(today)&&DateUtil.date().isBefore(endTime)&&flag==false){
                status = "";
            }
            HashMap map = new HashMap();
            map.put("date", date);
            map.put("type", type);
            map.put("status", status);
            map.put("day", one.dayOfWeekEnum().toChinese("周"));
            list.add(map);
        });
        return list;
    }

    @Override
    public ArrayList<HashMap> searchMonthCheckin(HashMap param) {
        return this.searchWeekCheckin(param);
    }
}
