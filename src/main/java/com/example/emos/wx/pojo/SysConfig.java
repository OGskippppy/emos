package com.example.emos.wx.pojo;

import java.io.Serializable;
import lombok.Data;

/**
 * @TableName sys_config
 */
@Data
public class SysConfig implements Serializable {
    private Integer id;

    private String paramKey;

    private String paramValue;

    private Integer status;

    private String remark;

    private static final long serialVersionUID = 1L;
}