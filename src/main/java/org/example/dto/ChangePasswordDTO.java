package org.example.dto;

import lombok.Data;

//用于层与层之间传输数据，比如前端传给后端
//配置修改密码
@Data
public class ChangePasswordDTO {
    private String oldPassword;
    private String newPassword;
}
