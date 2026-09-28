package org.example.service;

import org.example.dto.UserLoginDTO;
import org.example.dto.UserRegisterDTO;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.example.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;


@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;//导入server实列

    @Autowired
    private JwtUtil jwtUtil;


    // 密码加密器（BCrypt自带加盐，比MD5安全1万倍）
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // ---------- 注册 ----------
    public void register(UserRegisterDTO dto) {
        // 1. 检查用户名是否已被占用
        if (userMapper.selectByUsername(dto.getUsername()) != null) {
            throw new RuntimeException("用户名已存在"); // 正式项目用自定义异常
        }
        // 2. 验证码校验（固定为 123456）
        if (!"123456".equals(dto.getCaptcha())) {
            throw new RuntimeException("验证码错误");
        }

        // 2. 创建新用户，密码加密后存入
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(encoder.encode(dto.getPassword())); // 加密！

        // 3. 插入数据库
        userMapper.insert(user);

    }

    // ---------- 登录 ----------
    public UserLoginDTO login(UserLoginDTO dto) {
        // 1. 根据用户名查用户
        User user = userMapper.selectByUsername(dto.getUsername());
        System.out.print("=== 登录调试 ===");
        System.out.print("输入用户名: " + dto.getUsername());
        System.out.print("数据库密码密文: " + (user != null ? user.getPassword() : "null"));
        if (user != null) {
            System.out.print("密码比对结果: " + encoder.matches(dto.getPassword(), user.getPassword()));
        }

        // 2. 判空 + 验证密码 （注意：用 matches，不是 equals！）
        if (user == null || !encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        // 3. 生成JWT Token
        String token = jwtUtil.generateToken(user.getId().toString());

        // 4. 把数据塞回 dto 返回（这就是前端需要的完整数据！）
        dto.setId(user.getId());
        dto.setPassword(null); // ⚠️ 强烈建议：千万别把密码返回给前端，置空！
        dto.setToken(token);
        return dto;
    }

    // ---------- 账户管理 ----------
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        // 1. 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 验证旧密码
        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 3. 加密新密码并更新
        user.setPassword(encoder.encode(newPassword));
        userMapper.updateById(user);
    }
        //  修改用户名（需校验唯一性）
        public void changeUsername(Long userId, String newUsername) {
            // 1. 检查用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 2. 检查新用户名是否被占用（排除自己）
            User existing = userMapper.selectByUsername(newUsername);
            if (existing != null && !existing.getId().equals(userId)) {
                throw new RuntimeException("用户名已被占用");
            }

            // 3. 更新用户名
            user.setUsername(newUsername);
            userMapper.updateById(user);
        }

    /**
     * 获取当前登录用户信息（根据ID）
     */
    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 密码脱敏（返回前将密码置空，避免泄露）
        user.setPassword(null);
        return user;
    }

    /**
     * 获取当前登录用户信息（从请求中解析 token 获取 userId）
     * 通常由 Controller 调用
     */
    public User getCurrentUser(HttpServletRequest request) {
        String userIdStr = jwtUtil.getUserIdFromRequest(request);
        if (userIdStr == null) {
            throw new RuntimeException("未登录或Token无效");
        }
        Long userId = Long.parseLong(userIdStr);
        return getUserById(userId);
    }

    // 新增方法：根据用户名查用户
    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    // 新增方法：批量查询用户
    public List<User> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return userMapper.selectBatchIds(ids);
    }
}