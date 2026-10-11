package com.example.emos.wx.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.emos.wx.pojo.TbUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface UserService extends IService<TbUser> {

    public String getOpenId(String code);

    public Integer registerUser(String registerCode,String code,String nickname,String photo);

    public Set<String> searchUserPermissions(int userId);

    public Integer login(String code);
    public TbUser searchById(Integer  userId);
    public String searchUserHiredate(int userId);

    public HashMap searchUserSummary(int userId);
    public ArrayList<HashMap> searchUserGroupByDept(String keyword);
    public ArrayList<HashMap> searchMembers(ArrayList params);
    public ArrayList<HashMap> selectUserPhotoAndName(ArrayList<Integer> param);
    public List<String> searchUserRoleNames(int userId);
}
