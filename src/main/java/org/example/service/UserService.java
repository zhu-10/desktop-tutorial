package org.example.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.dto.UserLoginDTO;
import org.example.dto.UserRegisterDTO;
import org.example.dto.userDTO;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.example.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Autowired
    private UserMapper userMapper; // 导入server实列

    @Autowired
    private JwtUtil jwtUtil;

    // 密码加密器（BCrypt自带加盐，比MD5安全1万倍）
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // ---------- 注册 ----------
    public void register(UserRegisterDTO dto) {
        if (userMapper.selectByUsername(dto.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        if (!"123456".equals(dto.getCaptcha())) {
            throw new RuntimeException("验证码错误");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(encoder.encode(dto.getPassword()));
        userMapper.insert(user);
    }

    // ---------- 登录 ----------
    public UserLoginDTO login(UserLoginDTO dto) {
        User user = userMapper.selectByUsername(dto.getUsername());
        System.out.print("=== 登录调试 ===");
        System.out.print("输入用户名: " + dto.getUsername());
        System.out.print("数据库密码密文: " + (user != null ? user.getPassword() : "null"));
        if (user != null) {
            System.out.print("密码比对结果: " + encoder.matches(dto.getPassword(), user.getPassword()));
        }

        if (user == null || !encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId().toString());
        dto.setId(user.getId());
        dto.setPassword(null);
        dto.setToken(token);
        return dto;
    }

    // ---------- 账户管理 ----------
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(encoder.encode(newPassword));
        userMapper.updateById(user);
    }

    // 修改用户名（需校验唯一性）
    public void changeUsername(Long userId, String newUsername) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        User existing = userMapper.selectByUsername(newUsername);
        if (existing != null && !existing.getId().equals(userId)) {
            throw new RuntimeException("用户名已被占用");
        }

        user.setUsername(newUsername);
        userMapper.updateById(user);
    }

    /**
     * ⚠️ 补充：根据用户ID查询用户（密码脱敏）
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
     * 获取当前登录用户信息
     */
    public User getCurrentUser(javax.servlet.http.HttpServletRequest request) {
        // 1. 从请求中获取 userId
        String userIdStr = jwtUtil.getUserIdFromRequest(request);
        if (userIdStr == null) {
            throw new RuntimeException("未登录或Token无效");
        }

        // 2. 转换为 Long
        Long userId = Long.parseLong(userIdStr);

        // 3. ⚠️ 修正：调用 getUserById 方法，而不是 changePassword
        return getUserById(userId);
    }

    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    /**
     * 修改用户资料
     */
    public void updateUser(Long userId, userDTO dto) {
        // 1. 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 手机号唯一性校验
        if (dto.getPhone() != null && !dto.getPhone().trim().isEmpty() && !dto.getPhone().equals(user.getPhone())) {
            User existing = userMapper.selectByPhone(dto.getPhone());
            if (existing != null && !existing.getId().equals(userId)) {
                throw new RuntimeException("该手机号已被其他账号绑定");
            }
            user.setPhone(dto.getPhone());
        }

        // 3. 逐一更新非空字段（加上了 .trim() 避免前端传空字符串覆盖原有数据）
        if (dto.getRemark() != null && !dto.getRemark().trim().isEmpty()) user.setRemark(dto.getRemark());
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) user.setEmail(dto.getEmail());
        if (dto.getHobby() != null && !dto.getHobby().trim().isEmpty()) user.setHobby(dto.getHobby());
        if (dto.getSignature() != null && !dto.getSignature().trim().isEmpty()) user.setSignature(dto.getSignature());
        if (dto.getGender() != null && !dto.getGender().trim().isEmpty()) user.setGender(dto.getGender());

        // 特殊处理日期：防止前端传空字符串 "" 导致数据库 Date 类型报错
        if (dto.getBirthday() != null && !dto.getBirthday().trim().isEmpty()) {
            user.setBirthday(dto.getBirthday());
        }

        // 4. 持久化到数据库
        userMapper.updateById(user);
    }
}