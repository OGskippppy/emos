package com.example.emos.wx.pojo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Data;

/**
 * @TableName tb_meeting
 */
@Data
public class TbMeeting implements Serializable {
    private Long id;

    private String uuid;

    private String title;

    private Long creatorId;

    private String date;

    private String place;

    private String start;

    private String end;

    private Integer type;

    private Object members;

    private String desc;

    private String instanceId;

    private Integer status;

    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}