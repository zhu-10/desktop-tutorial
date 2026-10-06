package org.example.controller;

import org.example.dto.*;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.example.service.UserService;
import org.example.utils.JwtUtil;
import org.example.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;      // 直接注入 Mapper，用于查数据库

    @Autowired
    private JwtUtil jwtUtil;            // 注入 JWT 工具类


    // 注册：POST /shu/register
    @PostMapping("/register")
    public Result register(@RequestBody UserRegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功");
    }

    // 登录：POST /shu/login
    @PostMapping("/login")
    public Result<UserLoginDTO> login(@RequestBody UserLoginDTO dto) {
        UserLoginDTO result = userService.login(dto);
        return Result.success(result);
    }

    // 获取当前用户信息：GET /shu/username
    @GetMapping("/username")
// 🟢 1. 增加接收参数 user_id（不是必须传，所以 required = false）
    public Result getCurrentUser(@RequestParam(value = "user_id", required = false) String queryUserId) {

        // 2. 如果前端传了 user_id，说明是搜索别人
        if (queryUserId != null && !queryUserId.trim().isEmpty()) {
            // 这里需要根据用户名查询（假设 queryUserId 传的是用户名 "rooter238"）
            //  注意：你需要在 UserMapper 里加上 selectByUsername 方法！
            User targetUser = userMapper.selectByUsername(queryUserId);
            if (targetUser == null) {
                return Result.error("用户不存在");
            }
            UserInfoDTO dto = new UserInfoDTO();
            dto.setId(targetUser.getId());         // 返回真实的数字 ID（比如 2）
            dto.setUsername(targetUser.getUsername()); // 返回真实用户名
            return Result.success(dto);
        }

        // 3. 如果前端没传 user_id，说明是获取自己信息（保留原逻辑）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error("未登录或 Token 无效");
        }
        String userId = (String) authentication.getPrincipal();
        if (userId == null) {
            return Result.error("用户 ID 无效");
        }

        long id;
        try {
            id = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return Result.error("用户ID格式错误");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return Result.success(dto);
    }

    // 修改密码
    @PutMapping("/password")
    public Result changePassword(
            @RequestBody ChangePasswordDTO dto,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        userService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return Result.success("密码修改成功");
    }

    // 修改用户信息
    @PutMapping("/user")
    public Result updateUserInfo(
            @RequestBody userDTO dto,
            HttpServletRequest request) {

        // 获取当前登录用户的 ID (这里延用你原有的 getCurrentUserId)
        Long userId = getCurrentUserId(request);

        // 调用业务层更新方法
        userService.updateUser(userId, dto);

        return Result.success("用户信息修改成功");
    }
    // 辅助方法
    // ==========================================
    private Long getCurrentUserId(HttpServletRequest request) {
        String userIdStr = jwtUtil.getUserIdFromRequest(request);
        if (userIdStr == null) {
            // 优化：不要返回 null，直接抛出异常，防止后续空指针
            throw new RuntimeException("未登录或Token无效");
        }
        return Long.parseLong(userIdStr);
    }

    // 获取当前用户信息
    @GetMapping("/info")
    public Result<User> getCurrentUser(HttpServletRequest request) {
        // ⚠️ 重点：这里绝对不需要 @RequestBody，也不需要 userDTO 参数
        User currentUser = userService.getCurrentUser(request);
        // 直接返回查询到的用户信息
        return Result.success(currentUser);
    }

}