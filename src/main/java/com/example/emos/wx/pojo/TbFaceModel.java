package com.example.emos.wx.pojo;

import java.io.Serializable;
import lombok.Data;

/**
 * @TableName tb_face_model
 */
@Data
public class TbFaceModel implements Serializable {
    private Integer id;

    private Integer userId;

    private String faceModel;

    private static final long serialVersionUID = 1L;
}