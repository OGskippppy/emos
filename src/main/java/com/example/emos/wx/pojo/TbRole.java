package com.example.emos.wx.pojo;

import java.io.Serializable;
import lombok.Data;

/**
 * @TableName tb_role
 */
@Data
public class TbRole implements Serializable {
    private Integer id;

    private String roleName;

    private Object permissions;

    private static final long serialVersionUID = 1L;
}