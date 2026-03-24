package com.example.emos.wx.pojo;

import java.io.Serializable;
import lombok.Data;

/**
 * @TableName tb_city
 */
@Data
public class TbCity implements Serializable {
    private Integer id;

    private String city;

    private String code;

    private static final long serialVersionUID = 1L;
}