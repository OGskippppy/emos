package com.example.emos.wx.mapper;

import com.example.emos.wx.pojo.TbMeeting;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author Li
* @description 针对表【tb_meeting(会议表)】的数据库操作Mapper
* @createDate 2025-07-31 20:02:01
* @Entity com.example.emos.wx.pojo.TbMeeting
*/
@Mapper
public interface TbMeetingMapper extends BaseMapper<TbMeeting> {

    /**
     * 插入会议记录
     * @param meeting 会议实体
     * @return 影响行数
     */
    public int insertMeeting(TbMeeting meeting);
    public ArrayList<HashMap> searchMyMeetingListByPage(HashMap param);

    public Boolean searchMeetingMembersInSameDept(String uuid);
    public int updateMeetingInstanceId(HashMap param);

    public HashMap searchMeetingById(int id);
    public ArrayList<HashMap> searchMeetingMembers(int id);


    int updateMeetingInfo(HashMap param);

    public int deleteMeetingById(int id);
}




