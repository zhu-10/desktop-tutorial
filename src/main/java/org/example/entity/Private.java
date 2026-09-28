package org.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;


//私信字段
@Data
@TableName("private")  // 反引号避免关键字冲突
public class Private {
    @TableId(type = IdType.AUTO)
    private Long id;           // 新增独立主键
    @JsonProperty("sender_id")
    private Long sender;     // 发送者ID（字符串或Long）
    @JsonProperty("receiver_id")//前端传 receiver_id 或者 receiver 都能识别
    private Long receiver;   // 接收者ID
    private String content;     //私信
    private Integer isRead;     // 建议改成 isRead，符合驼峰是否已读
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;    //创建时间
}
