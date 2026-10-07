package org.example.controller;


import lombok.extern.slf4j.Slf4j;
import org.example.dto.FriendDto;
import org.example.entity.Friend;
import org.example.exception.BizException;
import org.example.mapper.FriendMapper;
import org.example.service.FriendService;
import org.example.utils.Result;
import org.example.utils.SecurityUtils;
import org.example.vo.FriendVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shu")
public class FriendController {
    @Autowired
    private FriendService friendService;

    @Autowired
    private FriendMapper friendmapper;


    //发送好友申请
    @PostMapping("/friends/{requestId}")
    public Result<Void> add(@RequestBody @Valid FriendDto dto) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        friendService.addFriend(currentUserId, dto.getUserId());
        return Result.success(); //  不用传 null 了，直接无参
    }

    // 获取好友列表
    @GetMapping("/friend")
    public List<FriendVo> list(Long currentUserId, Integer page, Integer pageSize) {
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        int offset = (page - 1) * pageSize;

        log.info("listFriends currentUserId={}, page={}, pageSize={}", currentUserId, page, pageSize);
        //关键点：MySQL 的 LIMIT 是 LIMIT offset, size，不是 LIMIT page, size。所以要把 page 换算成 offset
        List<FriendVo> list = friendmapper.listFriends(currentUserId, offset, pageSize);
        log.info("listFriends result size = {}", list.size());
        return list;
    }

    // 删除好友
    @DeleteMapping("/remove/{requestId}")
    //注意，使用@PathVariable("requestId")需要在vo加上id，让前端获取id
    public Result<Void> delete(@PathVariable("requestId") Long userId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        friendService.deleteFriend(currentUserId, userId);
        return Result.success();
    }

    // 1. 获取好友申请列表
    @GetMapping("/friendship")
    public Result<List<FriendVo>> getFriendRequests() {
        Long currentUserId = SecurityUtils.getCurrentUserId(); // 从token获取
        List<FriendVo> list = friendService.getFriendRequests(currentUserId);
        return Result.success(list); // 🟢 统一包装
    }

    // 2. 同意好友申请
    @PostMapping("/handle/{requestId}")
    public Result<Void> agreeFriend(@PathVariable("requestId") Long applyId) {
        Long currentUserId =SecurityUtils.getCurrentUserId();
        friendService.acceptFriendRequest(currentUserId, applyId);
        return Result.success();
    }

    // 3. 删除/拒绝好友申请
    @DeleteMapping("/refuse/{requestId}")
    public Result<Void> deleteFriendRequest(@PathVariable("requestId") Long requestId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        friendService.deleteFriendRequest(requestId, currentUserId);
        return Result.success();
    }


    //发送消息
    @PostMapping("/messages")
    public Result<Integer> insertMessage(@RequestBody Friend friend) {
        // 1. 获取当前登录用户
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 2. 参数校验
        if (friend == null || friend.getFriend() == null) {
            throw new BizException("接收者不能为空");
        }

        // 3. 强制设置发送者为当前登录用户，防止前端伪造发送者
        friend.setMyself(currentUserId);

        // 4. 调用 Service 层插入消息
        int result = friendService.insertMessage(friend);

        // 5. 返回统一结果
        return Result.success(result);
    }
    //获取消息
    @GetMapping("/getmessages")
    public Result<List<FriendVo>> getMessages(
            @RequestParam Long userId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {

        // 1. 获取当前登录用户（强制作为 myId，防止越权查看他人聊天记录）
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 2. 参数校验
        if (userId == null) {
            throw new BizException("好友ID不能为空");
        }
        // 3. 调用 Service 层获取消息（注意：分页计算 offset 的逻辑在 Service 层处理）
        List<FriendVo> messages = friendService.getMessages(currentUserId, userId, page, pageSize);

        // 4. 返回统一结果
        return Result.success(messages);
    }

    //标记已读
    @PostMapping("/isread")
    public Result<Void> markRead(@RequestBody Map<String, Long> payload) {
        // 1. 从 Token 获取当前登录用户（安全！）
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 2. 从 JSON 中提取前端传来的 userId
        Long userId = payload.get("userId");
        if (userId == null) {
            throw new BizException("好友ID不能为空");
        }

        // 3. 调用 Service
        friendService.markRead(currentUserId, userId);

        return Result.success();
    }
}
