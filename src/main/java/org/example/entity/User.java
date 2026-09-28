//创建字段文件

package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user") // 指定数据库表名
public class User {
    @TableId(type = IdType.AUTO) // 主键自增
        private Long id;
        private String username;
        private String password; // 存加密后的密文，绝不存明文！
        private String phone;
        private String hobby;   //爱好
        private String gender;  //性别
        private String birthday;    //生日
}