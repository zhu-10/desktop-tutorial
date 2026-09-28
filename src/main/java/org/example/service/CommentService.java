package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.Comment;
import org.example.vo.CommentVo;
import java.util.List;


// CommentService.java
public interface CommentService extends IService<Comment> {
    //分页查询评论列表
    List<Comment> pageList(Integer page, Integer pageSize);
    //获取用户名，评论页面需要获取用户名
    List<Comment> listByUsername(String username);
    //查询评论及其用户名
    List<CommentVo> getCommentsWithUser(Long shuId, Integer page, Integer size);
    //保存评论，设置/校验当前用户
    boolean saveComment(Comment comment, Long currentUserId);
}
