package org.example.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;


//评论字段
@Data
@TableName("comment")
public class Comment {
    private Long id;
    private Long userId;   //用户名
    private String comment; //评论
    private Long shuId; //获取用户名
    @TableField(fill = FieldFill.INSERT)//插入自动填充
    private LocalDateTime createTime;//发布时间

}
