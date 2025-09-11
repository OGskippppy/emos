package com.example.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONUtil;
import com.example.emos.wx.controller.form.TestForm;
import com.example.emos.wx.pojo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@Api(tags = "测试Web接口")
public class TestController {

    @ApiOperation("测试接口")
    @PostMapping("/hello")
    public Result test(@Valid @RequestBody TestForm form){
        String msg = form.getName() +"测试成功！";
        return Result.success(msg);
    }
}