package com.example.emos.wx.pojo;

import java.io.Serializable;
import lombok.Data;

/**
 * @TableName tb_permission
 */
@Data
public class TbPermission implements Serializable {
    private Integer id;

    private String permissionName;

    private Integer moduleId;

    private Integer actionId;

    private static final long serialVersionUID = 1L;
}