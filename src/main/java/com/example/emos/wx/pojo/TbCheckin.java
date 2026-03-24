package com.example.emos.wx.pojo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Data;

/**
 * @TableName tb_checkin
 */
@Data
public class TbCheckin implements Serializable {
    private Integer id;

    private Integer userId;

    private String address;

    private String country;

    private String province;

    private String city;

    private String district;

    private Integer status;

    private Integer risk;

    private String date;

    private Date createTime;

    private static final long serialVersionUID = 1L;
}