package org.example.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;


//配置jwt认证
@Component
public class JwtUtil {

    private final long EXPIRATION_TIME = 86400000; // 24小时，单位毫秒
    @Value("${jwt.secret}")
    private String secretKey;   // 从配置文件读取

    // 生成 Token
    public String generateToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes()) // 注意：secretKey 要转成 byte[]
                .compact();   // ✅ 必须调用 compact() 生成三段式 JWT
    }

    // 从 Token 中解析用户 ID
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey.getBytes())
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            // 解析失败返回 null
            System.out.print("Token 解析失败: " + e.getMessage());
            return null;
        }
    }

    public String getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return getUserIdFromToken(token);
    }
}