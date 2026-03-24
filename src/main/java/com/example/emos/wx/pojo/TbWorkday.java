package com.example.emos.wx.pojo;

import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;

/**
 * @TableName tb_workday
 */
@Data
public class TbWorkday implements Serializable {
    private Integer id;

    private LocalDate date;

    private static final long serialVersionUID = 1L;
}