package org.example.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendVo {
    private Long id;    // 🟢 申请记录的主键
    private Long userId;  //好友id
    private String friendName;  //好友名
    //private String friendAvatar;    //头像
    private String content;     // 消息
    private Boolean isRead; //已读
    private LocalDateTime updateTime;   //更新时间
}
