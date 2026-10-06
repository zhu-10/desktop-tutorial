package org.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.Comment;
import org.example.entity.User;
import org.example.mapper.CommentMapper;
import org.example.service.CommentService;
import org.example.service.UserService;
import org.example.vo.CommentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private UserService userService;  // ⭐ 注入 UserService

    // 1. 分页查询所有评论（按时间倒序）
    @Override
    public List<Comment> pageList(Integer page, Integer pageSize) {
        Page<Comment> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Comment::getCreateTime);
        return baseMapper.selectPage(pageParam, wrapper).getRecords();
    }

    // 2. 根据用户名查询评论（如果 Comment 实体有 username 字段的话）
    //    这里我们假设 Comment 没有 username，只有 userId，所以需要先查用户ID再查评论
    //    如果您的 Comment 确实有 username 字段，请使用 Comment::getUsername
    @Override
    public List<Comment> listByUsername(String username) {

        // 1. 先根据用户名查用户（⚠️改成 getByUsername）
        // 如果你 UserService 里没有这个方法，需要去 UserService 里加上：
        // public User getByUsername(String username) { return userMapper.selectByUsername(username); }
        User user = userService.getByUsername(username);

        if (user == null) {
            return Collections.emptyList();
        }

        // 2. 再根据 userId 查评论
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getUserId, user.getId())
                .orderByDesc(Comment::getCreateTime);
        return baseMapper.selectList(wrapper);
    }

    // 3. 保存评论（设置当前用户ID）
    @Override
    public boolean saveComment(Comment comment, Long currentUserId) {
        comment.setUserId(currentUserId);
        // 如果前端没有传 shuId，可以从请求参数中获取，这里保留字段由前端传入
        return save(comment);
    }

    // 4. 根据 shuId 获取评论列表，并附带用户名和头像（批量查询，避免 N+1）
    @Override
    public List<CommentVo> getCommentsWithUser(Long shuId, Integer page, Integer size) {
        // 分页查询评论
        Page<Comment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getShuId, shuId)
                .orderByDesc(Comment::getCreateTime);
        List<Comment> comments = baseMapper.selectPage(pageParam, wrapper).getRecords();
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        // 提取所有 userId
        List<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .collect(Collectors.toList());

        // 批量查询用户信息
        List<User> users = userService.listByIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 组装 VO
        return comments.stream().map(comment -> {
            CommentVo vo = new CommentVo();
            BeanUtils.copyProperties(comment, vo);   // 复制 id, comment, createTime
            User user = userMap.get(comment.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                // 如果 User 有 avatar 字段，取消注释；否则注释掉
                // vo.setAvatar(user.getAvatar());
            }
            return vo;
        }).collect(Collectors.toList());
    }
}