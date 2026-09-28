package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

//配置好友字段
@Data
@TableName("Friend")
public class Friend {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer status; //待处理
    private Long friend;    //好友
    private Long myself;    //自己
    private String content; //消息
    private Boolean isRead; //已读
    @TableField
    private LocalDateTime createTime;   //创建时间
    private LocalDateTime updateTime;   //最新消息时间
}
