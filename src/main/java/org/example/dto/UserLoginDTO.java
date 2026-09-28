package org.example.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;


//登录
@Data
public class UserLoginDTO {
    private Long id; // ⭐ 新增：用来返回给前端用户ID
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    private String token;// JWT令牌

}