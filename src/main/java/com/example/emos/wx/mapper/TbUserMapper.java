package com.example.emos.wx.mapper;

import com.example.emos.wx.pojo.TbUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
* @author Li
* @description 针对表【tb_user(用户表)】的数据库操作Mapper
* @createDate 2025-07-31 20:02:01
* @Entity com.example.emos.wx.pojo.TbUser
*/
@Mapper
public interface TbUserMapper extends BaseMapper<TbUser> {

    Boolean selectRootCount();

    void insert(@Param("param") Map<String, Object> param);

    Set<String> searchUserPermissions(@Param("userId") int userId);

    Integer searchIdByOpenId( String openId);

    public TbUser  searchById(Integer  userId);

    public String searchUserHiredate(int userId);

    public HashMap searchUserSummary(int userId);

    public ArrayList<HashMap> searchUserGroupByDept(String keyword);

    public ArrayList<HashMap> searchMembers(ArrayList params);

    public HashMap searchUserInfo(int userId);

    public int searchDeptManagerId(int id);

    public int searchGmId();
    
    public ArrayList<HashMap> selectUserPhotoAndName(ArrayList<Integer> param);
}




