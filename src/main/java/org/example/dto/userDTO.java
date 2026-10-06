package org.example.dto;
import lombok.Data;

@Data
public class userDTO {
    private String phone;      // 电话
    private String remark;     // 备注
    private String email;      // 邮箱
    private String hobby;      // 爱好
    private String signature;  // 简介
    private String gender;     // 性别
    private String birthday;   // 生日
}