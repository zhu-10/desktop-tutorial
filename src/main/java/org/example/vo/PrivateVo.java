package org.example.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PrivateVo {
    private Long id;
    private Long senderId;   // 发送者ID
    private Long receiverId; // 接收者ID
    private String senderName;      // 发送者名
    private String receiverName;    // 接收者名     // 发送者用户名（新增，用于前端展示）
    private String content;       // 私信内容（你提到的评论数据）

    // 格式化时间，前端才能正常显示 "2023-10-01 12:00:00"，而不是时间戳或数组
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime; // 时间

    private Integer isRead;       // 是否已读
}