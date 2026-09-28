package org.example.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

//前端发送给后端字段
@Data
public class FriendDto {
    @NotNull(message = "好友ID不能为空")
    private Long userId; //  传真正的数字ID
    private String friendName;  //好友名
    private String remark;      // 备注，可存到 content 或单独字段
}
