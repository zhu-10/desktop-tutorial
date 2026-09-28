package org.example.controller;


import org.example.entity.Comment;
import org.example.service.CommentService;

import org.example.service.UserService;
import org.example.utils.JwtUtil;
import org.example.utils.Result;
import org.example.vo.CommentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/shu")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    //发送评论数据
    @PostMapping("/comment")
    public Result addComment(@RequestBody Comment comment,
                             @RequestParam Long shuId,   // 从请求参数获取
                             HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        comment.setShuId(shuId);  // 显式设置
        boolean ok = commentService.saveComment(comment, currentUserId);
        return ok ? Result.success("评论成功") : Result.error("评论失败");
    }

    //获取评论
    @GetMapping("/Obtain")
    public Result getChat(@RequestParam Long shuId,
                          @RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size) {
        List<CommentVo> list = commentService.getCommentsWithUser(shuId, page, size);
        return Result.success(list);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String userIdStr = jwtUtil.getUserIdFromRequest(request);
        if (userIdStr == null || userIdStr.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
