package com.example.emos.wx.pojo;

import java.io.Serializable;
import lombok.Data;

/**
 * @TableName tb_action
 */
@Data
public class TbAction implements Serializable {
    private Integer id;

    private String actionCode;

    private String actionName;

    private static final long serialVersionUID = 1L;
}