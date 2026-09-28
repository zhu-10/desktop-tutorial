package org.example.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 获取当前登录用户信息的工具类
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户的 ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 防御性判断：防止请求绕过了 JwtAuthenticationFilter 导致空指针
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("请先登录");
        }

        // 因为我们在 JwtAuthenticationFilter 里设置的是 authentication.setName(userId)
        // 而且 JWT 里的 userId 是字符串，所以这里需要转成 Long
        return Long.valueOf(authentication.getName());
    }
}