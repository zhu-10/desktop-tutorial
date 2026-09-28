package org.example.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


//注册
@Data
public class UserRegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 1, max = 20, message = "用户名1-20位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 8, message = "密码必须是8位")
    private String password;

    @NotBlank(message = "验证码不能为空")
    @Size(min = 6,max = 6,message = "验证码错误")
    @Pattern(regexp = "\\d{6}", message = "验证码必须为6位数字")
    private String captcha;   // 用户输入的验证码


}