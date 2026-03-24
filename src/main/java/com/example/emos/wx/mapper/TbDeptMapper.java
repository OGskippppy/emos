package com.example.emos.wx.mapper;

import com.example.emos.wx.pojo.TbDept;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

/**
* @author Li
* @description 针对表【tb_dept】的数据库操作Mapper
* @createDate 2025-07-31 20:02:01
* @Entity com.example.emos.wx.pojo.TbDept
*/
@Mapper
public interface TbDeptMapper extends BaseMapper<TbDept> {
    public ArrayList<HashMap> searchDeptMembers(String keyword );

}




