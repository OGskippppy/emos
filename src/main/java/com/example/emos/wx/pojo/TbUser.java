package com.example.emos.wx.pojo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * @TableName tb_user
 */
@Data
public class TbUser implements Serializable {
    private Integer id;

    private String openId;

    private String nickname;

    private String photo;

    private String name;

    private Object sex;

    private String tel;

    private String email;

    private LocalDate hiredate;

    private Object role;

    private Integer root;

    private Integer deptId;

    private Integer status;

    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}