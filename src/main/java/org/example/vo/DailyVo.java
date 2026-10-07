package org.example.vo;

import lombok.Data;

import java.util.Date;

@Data
public class DailyVo {
    private Long userId;//发布者id
    private String username;//发布者名字
    private String theme;//主题
    private String content;//内容
    private String imageUrl;//图片
    /** 0-纯文本 1-图文 2-纯图片 */
    private Integer type;      // 对应 type

}
