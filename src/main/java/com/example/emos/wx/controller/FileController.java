package com.example.emos.wx.controller;

import com.example.emos.wx.pojo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/file")
@Slf4j
@Api(tags = "文件上传接口")
public class FileController {

    @ApiOperation("上传图片")
    @PostMapping("/upload")
    public Result uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只能上传图片文件");
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;

            // 创建上传目录
            String uploadDir = "uploads/images/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            File dest = new File(uploadDir + filename);
            file.transferTo(dest);

            // 返回文件URL
            String fileUrl = "/emos-wx-api/file/images/" + filename;
            
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败");
        }
    }

    @ApiOperation("获取图片")
    @GetMapping("/images/{filename}")
    public void getImage(@PathVariable String filename, 
                        javax.servlet.http.HttpServletResponse response) throws IOException {
        File file = new File("uploads/images/" + filename);
        if (file.exists()) {
            response.setContentType("image/jpeg");
            java.nio.file.Files.copy(file.toPath(), response.getOutputStream());
        } else {
            response.setStatus(404);
        }
    }
} 