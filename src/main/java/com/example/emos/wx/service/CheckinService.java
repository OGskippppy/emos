package com.example.emos.wx.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.emos.wx.controller.form.CheckinForm;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public interface CheckinService {
    public String validCanCheckIn(int userId,String date);

    public void checkin(HashMap param);

    public void createFaceModel(int userId,String path);

    public HashMap searchTodayCheckin(int userId);
    public long searchCheckinDays(int userId);
    public ArrayList<HashMap> searchWeekCheckin(HashMap param);
    public ArrayList<HashMap> searchMonthCheckin(HashMap param);


}
