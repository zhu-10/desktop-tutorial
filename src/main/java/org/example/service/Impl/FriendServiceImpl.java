package org.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.Friend;
import org.example.entity.User;
import org.example.mapper.FriendMapper;
import org.example.mapper.UserMapper;
import org.example.service.FriendService;
import org.example.vo.FriendVo;
import org.example.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;   // 🟢 只 import 这个

@Slf4j
@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {
    @Autowired
    private FriendMapper friendmapper;
    @Autowired
    private UserMapper userMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)// 开启事务，保证一致性
    //@Transactional要么全部成功，要么全部失败。
    //rollbackFor = Exception.class指定触发回滚的异常类型，是一个标准且安全的开发习惯，能极大减少脏数据的产生
    //添加好友
    public void addFriend(Long currentUserId, Long userId) {
        // 1. 参数校验
        if (currentUserId == null) throw new BizException("请先登录");
        if (userId == null)      throw new BizException("好友ID不能为空");
        if (currentUserId.equals(userId)) throw new BizException("不能添加自己为好友");

        // 2. 对方是否存在
        User target = userMapper.selectById(userId);
        if (target == null) throw new BizException("用户不存在");

        // 3. 是否已经是好友（单向检查即可，正常情况双向同时存在）
        if (friendmapper.countRealFriend(currentUserId, userId) > 0) {
            throw new BizException("你们已经是好友了");
        }

        // 是否存在待处理申请
        if (friendmapper.countPending(currentUserId, userId) > 0) {
            throw new BizException("已发送申请，等待对方同意");
        }

        // 4. 双向插入两条记录
        //私信确实是双向的沟通，但每条消息本身是单向的，绝不能像好友那样存两行。真正需要双份的是会话视图
        LocalDateTime now = LocalDateTime.now();
        Friend apply = new Friend();
        apply.setMyself(currentUserId);   // 发送方 A
        apply.setFriend(userId);          // 接收方 B
        apply.setContent("");
        apply.setIsRead(false);
        apply.setStatus(0);               // 待处理
        apply.setCreateTime(now);
        apply.setUpdateTime(now);
        friendmapper.insert(apply);
    }


    //获取列表好友
    @Override
    public List<FriendVo> listFriends(Long currentUserId) {
        if (currentUserId == null) throw new BizException("请先登录");
        return friendmapper.selectFriendList(currentUserId, false);


    }
    //
    @Override
    //删除好友
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriend(Long currentUserId, Long friendId) {
        if (currentUserId == null) throw new BizException("请先登录");
        // 双向删除
        friendmapper.delete(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getMyself, currentUserId)
                .eq(Friend::getFriend, friendId));
        friendmapper.delete(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getMyself, friendId)
                .eq(Friend::getFriend, currentUserId));
    }

    @Override
    //更新好友消息
    public void updateLastMessage(Long myself, Long friend, String content, Boolean isRead) {
        friendmapper.updateLastMessage(myself, friend, content, isRead);
    }

    //好友申请列表
    @Override
    public List<FriendVo> getFriendRequests(Long currentUserId) {
        // 🟢 确认不是null
        log.info("当前登录用户ID: {}", currentUserId);
        // 🟢 直接调用 Mapper 里的自定义联表 SQL
        // 如果你只查未处理的申请，传 false；如果查全部，传 null
        List<FriendVo> list = friendmapper.selectFriendList(currentUserId, false);
        log.info("查询结果条数 = {}", list.size());   // 🟢 关键
        return list;
    }

    //同意好友申请
     @Override
     @Transactional(rollbackFor = Exception.class) // 开启事务，保证一致性
     //@Transactional要么全部成功，要么全部失败。
     //rollbackFor = Exception.class指定触发回滚的异常类型，是一个标准且安全的开发习惯，能极大减少脏数据的产生
     public void acceptFriendRequest(Long requestId, Long currentUserId) {
            // 1. 查询该申请记录
         if (currentUserId == null) throw new BizException("请先登录");
         if (requestId == null)       throw new BizException("申请ID不能为空");

         // 1. 查申请记录，校验权限和状态
         Friend apply = friendmapper.selectById(requestId);
         if (apply == null) throw new BizException("申请不存在");
         if (!currentUserId.equals(apply.getFriend()))
             throw new BizException("无权处理该申请");
         if (apply.getStatus() != 0)
             throw new BizException("该申请已处理");

         Long applicantId = apply.getMyself();   // 🟢 申请人 A
         LocalDateTime now = LocalDateTime.now();

         // 2. 把 A→B 这条改为"已同意、已读"
         friendmapper.acceptFriendRequest(requestId,now);
         // SQL: UPDATE friend SET status=1, is_read=1, update_time=#{updateTime} WHERE id=#{id}

         // 3. 反向插一条 B→A，表示好友关系建立
         Friend back = new Friend();
         back.setMyself(currentUserId);   // 🟢 我（同意的人 B）
         back.setFriend(applicantId);     // 🟢 申请人 A（不是 userId！）
         back.setContent("");
         back.setIsRead(true);            // 关系已建立，不算新通知
         back.setStatus(1);               // 已同意
         back.setCreateTime(now);
         back.setUpdateTime(now);
         friendmapper.insert(back);
    }

    //删除/拒绝好友申请
    // 逻辑：校验权限后直接删除
    @Override
    public void deleteFriendRequest(Long requestId, Long currentUserId) {
        // 1. 查询申请记录
        // 1. 查申请
        Friend apply = friendmapper.selectById(requestId);
        if (apply == null) throw new BizException("申请不存在");

        // 🟢 2. 校验权限：只能处理发给自己的申请！
        if (!currentUserId.equals(apply.getFriend())) {
            throw new BizException("无权处理该申请");
        }

        // 3. 校验状态（防止重复拒绝）
        if (apply.getStatus() != 0) {
            throw new BizException("该申请已处理");
        }
        //物理删除（直接删记录）
        friendmapper.deleteById(requestId);
    }

    //获取消息
    @Override
    public List<FriendVo> getMessages(Long myId, Long friendId, Integer page, Integer pageSize) {
        if (myId == null || friendId == null) throw new BizException("参数缺失");
        int p = (page == null || page < 1) ? 1 : page;
        int size = (pageSize == null || pageSize < 1) ? 20 : pageSize;
        int offset = (p - 1) * size;

        return friendmapper.listMessages(myId, friendId, offset, size);
    }

    //标记已读
    @Override
    public void markRead(Long myId, Long friendId) {
        if (myId == null || friendId == null) {
            throw new BizException("参数缺失");
        }

        LambdaUpdateWrapper<Friend> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Friend::getMyself, myId)          // 我
                .eq(Friend::getFriend, friendId)      // 对方
                .set(Friend::getIsRead, true);        // 已读 = 1
        // ⚠️ 不要 set updateTime，否则会话列表排序会乱

        this.update(wrapper);
    }

    //发送消息
    @Override
    @Transactional(rollbackFor = Exception.class) // 加上事务，保证插入失败时回滚
    public int insertMessage(Friend friend) {
        // 1. 参数校验
        if (friend == null || friend.getMyself() == null || friend.getFriend() == null) {
            throw new BizException("参数缺失：发送者和接收者不能为空");
        }
        if (friend.getContent() == null || friend.getContent().trim().isEmpty()) {
            throw new BizException("消息内容不能为空");
        }
        // 防止自己给自己发消息（视业务需求，如果允许可注释掉）
        if (friend.getMyself().equals(friend.getFriend())) {
            throw new BizException("不能给自己发送消息");
        }

        // 2. 赋予默认值
        friend.setIsRead(false); // 新消息默认未读
        friend.setStatus(0);     // 假设 0 代表正常消息，视你业务表设计而定
        LocalDateTime now = LocalDateTime.now();
        friend.setCreateTime(now); // 创建时间
        friend.setUpdateTime(now); // 最新消息时间（也可以与创建时间一致）

        // 3. 调用 Mapper 插入数据库
        return friendmapper.insertMessage(friend);
    }

}
