package com.example.emos.wx.mapper;

import com.example.emos.wx.pojo.SysConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author Li
* @description 针对表【sys_config】的数据库操作Mapper
* @createDate 2025-07-31 20:02:01
* @Entity com.example.emos.wx.pojo.SysConfig
*/
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {
    public List<SysConfig> selectAllParams();

}




