package com.example.emos.wx.mapper;

import com.example.emos.wx.pojo.TbHolidays;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author Li
* @description 针对表【tb_holidays(节假日表)】的数据库操作Mapper
* @createDate 2025-07-31 20:02:01
* @Entity com.example.emos.wx.pojo.TbHolidays
*/
@Mapper
public interface TbHolidaysMapper extends BaseMapper<TbHolidays> {
    public Integer seachTodayIsHoliday();

    ArrayList searchHolidaysInRange(HashMap param);
}




