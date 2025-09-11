package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@ApiModel
public class UpdateMeetingInfoForm {

    @NotBlank
    private String title;

    @NotNull
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$")
    private String date;

    private String place;
    @NotNull
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):([0-5][0-9])$")
    private String start;
    @NotNull
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):([0-5][0-9])$")
    private String end;

   @Range(min = 1,max = 2)
    private Byte type;

   @NotBlank
   private String members;

   @NotBlank
   private String desc;

   @Min(1)
   private Integer id;

   @NotBlank
    private String instanceId;


}
