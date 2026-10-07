package org.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

//配置发布字段
@Data
@TableName("daily")
public class Daily {
    @TableId(type = IdType.AUTO)//主键自增
    private Long id;
    @TableField("user_id")
    private Long userId;//发布者id
    private String username;//发布者名字
    private String theme;//主题
    private String content;//内容
    @TableField("image_url")
    private String image;   //图片
    /** 0-纯文本 1-图文 2-纯图片 */
    @TableField("msg_type")
    private Integer type;      // 对应 type
    private Integer isPublic;//是否公开
    @TableField(fill = FieldFill.INSERT)//插入自动填充
    private LocalDateTime createTime;//发布时间
    private LocalDateTime updateTime;//更新时间
}
