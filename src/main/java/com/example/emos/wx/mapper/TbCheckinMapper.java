package com.example.emos.wx.mapper;

import com.example.emos.wx.pojo.TbCheckin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.emos.wx.pojo.UserInfoDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author Li
* @description 针对表【tb_checkin(签到表)】的数据库操作Mapper
* @createDate 2025-07-31 20:02:01
* @Entity com.example.emos.wx.pojo.TbCheckin
*/
@Mapper
public interface TbCheckinMapper extends BaseMapper<TbCheckin> {
    public Integer haveCheckin(HashMap  param);

    public int insertCheckin(TbCheckin checkin);
    public HashMap searchTodayCheckin(int userId);
    public UserInfoDTO searchUserInfo(int userId);
    public long searchCheckinDays(int userId);
    public ArrayList<HashMap> searchWeekCheckin(HashMap param);


}




