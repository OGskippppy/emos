package com.example.emos.wx.mapper;

import com.example.emos.wx.pojo.TbWorkday;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author Li
* @description 针对表【tb_workday】的数据库操作Mapper
* @createDate 2025-07-31 20:02:01
* @Entity com.example.emos.wx.pojo.TbWorkday
*/
@Mapper
public interface TbWorkdayMapper extends BaseMapper<TbWorkday> {
    public Integer seachTodayIsWorkday();

    ArrayList searchWorkdayInRange(HashMap param);
}




