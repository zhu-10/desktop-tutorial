package org.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.entity.Private;
import org.example.service.PrivateService;
import org.example.utils.JwtUtil;
import org.example.utils.Result; // 假设你有统一返回类
import org.example.vo.PrivateVo;
import org.example.service.Impl.PrivateServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import java.util.List;


//私信请求
//MVC是M是模型，V是视图，Controller控制器
//@RestController自动序列化成json传给浏览器
@RestController
@RequestMapping("/shu")
public class PrivateController {

    @Autowired
    private PrivateServiceImpl privateServiceImpl;

    @Autowired
    private PrivateService privateService;

    @Autowired
    private JwtUtil jwtUtil;  // 用于解析 token 获取当前用户ID

    /**
     * 发送私信
     * POST /api/send
     * 请求体 JSON: { "receiver": "目标用户ID", "content": "消息内容" }
     * 注意：sender 由后端自动填充为当前登录用户
     */
    @PostMapping("/send")
    public Result sendPrivate(@RequestBody Private privateMessage, HttpServletRequest request) {
        // 1. 从请求中获取当前登录用户ID
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return Result.error("未登录，请先登录");
        }

        // 2. 调用 service 保存私信（会自动设置 sender）
        boolean success = privateService.savePrivate(privateMessage, currentUserId);
        if (success) {
            return Result.success("私信发送成功");
        } else {
            return Result.error("私信发送失败");
        }
    }

    // ====== 辅助方法：从 Request 中解析当前用户ID ======
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
    //获取私信数据
    @GetMapping("/chat")
    public Result<IPage<PrivateVo>> listByCondition(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long sender,
            @RequestParam(required = false) Long receiver,
            @RequestParam(required = false) Integer isRead,
            HttpServletRequest request) {

        // 1. 获取当前登录用户ID
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return Result.error("请先登录");
        }

        // 2. 调用 Service 查询（IPage 内部已经包含了 total）
        IPage<PrivateVo> pageResult = privateService.listByCondition(page, pageSize, sender, receiver, isRead, currentUserId);

        // 3. 直接返回 IPage
        return Result.success(pageResult);
    }

    //获取用户列表信息
    @GetMapping("/conversations")
    public Result<List<PrivateVo>> getConversations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(value = "page_size", defaultValue = "20") Integer pageSize,
            Authentication authentication) {

        String currentUserId = authentication.getName(); // principal 是 userId

        List<PrivateVo> list = privateServiceImpl.getPrivateMessages(currentUserId, page, pageSize);
        return Result.success(list);
    }

    //获取用户名和私信

    //配置标记已读
    @PostMapping("/mark")
    public Result<Void> markAsRead(@RequestParam Long userId,
                                   HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        //检查是否登录
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        privateService.markAsRead(userId, currentUserId);
        return Result.success(null);
    }
}