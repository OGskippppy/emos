package com.example.emos.wx.controller.form;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel
@Data
public class TestForm {

    @ApiModelProperty("姓名")
    private String name;
}
