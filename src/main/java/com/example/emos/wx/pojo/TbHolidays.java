package com.example.emos.wx.pojo;

import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;

/**
 * @TableName tb_holidays
 */
@Data
public class TbHolidays implements Serializable {
    private Integer id;

    private LocalDate date;

    private static final long serialVersionUID = 1L;
}