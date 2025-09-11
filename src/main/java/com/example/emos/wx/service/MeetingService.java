package com.example.emos.wx.service;

import com.example.emos.wx.pojo.TbMeeting;

import java.util.ArrayList;
import java.util.HashMap;

public interface MeetingService {
    public void insertMeeting(TbMeeting meeting);
    public ArrayList<HashMap> searchMyMeetingListByPage(HashMap param);
    public void startMeetingWorkFlow(TbMeeting meeting);
    public HashMap searchMeetingById(int id);

    public void updateMeetingInfo(HashMap  param);
    public int deleteMeetingById(int id);
}
