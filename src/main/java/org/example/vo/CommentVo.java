package org.example.vo;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

//专门用于前端页面展示，包含页面需要的所有字段
@Data
public class CommentVo {
    private Long id;
    private String comment;
    private LocalDateTime createTime;
    private String username;   // 从 User 表查出来的名字
    private String avatar;     // 可选
}
