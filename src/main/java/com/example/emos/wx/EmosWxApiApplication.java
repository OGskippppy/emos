package com.example.emos.wx;

import cn.hutool.core.util.StrUtil;
import com.example.emos.wx.constant.SystemConstant;
import com.example.emos.wx.mapper.SysConfigMapper;
import com.example.emos.wx.pojo.SysConfig;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

@Slf4j
@SpringBootApplication
@ServletComponentScan
@EnableAsync
@MapperScan("com.example.emos.wx.mapper")
public class EmosWxApiApplication {
    @Autowired
    private SystemConstant systemConstant;

    @Autowired
    private SysConfigMapper sysConfigMapper;

    @Value("${emos.image-folder}")
    private String imageFloder;

    public static void main(String[] args) {
        SpringApplication.run(EmosWxApiApplication.class, args);
    }


    @PostConstruct
    public void init() {
        List<SysConfig> sysConfigs = sysConfigMapper.selectAllParams();
        sysConfigs.forEach(one ->{
            String key = one.getParamKey();
            key= StrUtil.toCamelCase( key);
            String value = one.getParamValue();
            try {
                Field field = systemConstant.getClass().getDeclaredField(key);
                field.set(systemConstant, value);
            } catch (Exception e) {
                log.error("初始化系统参数失败: key={}, value={}", key, value, e);
            }
        } );

        new File(imageFloder).mkdirs();
    }

}
