package com.example.emos.wx.constant;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class SystemConstant {
    public String attendanceStartTime = "09:00";

    public String attendanceTime = "09:00";

    public String attendanceEndTime = "09:30";
    
    public String closingStartTime = "17:30";

    public String closingTime = "18:00";

    public String closingEndTime = "18:30";
}

