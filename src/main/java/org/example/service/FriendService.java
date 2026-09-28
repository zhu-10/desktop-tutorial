package org.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;
import org.example.entity.Friend;
import org.example.vo.FriendVo;
import org.example.vo.PrivateVo;

import java.util.List;


public interface FriendService extends IService<Friend> {
    /** 添加好友（双向建立关系） */
    void addFriend(Long currentUserId, Long userId);

    /** 获取我的好友列表 */
    List<FriendVo> listFriends(Long currentUserId);

    /** 删除好友（双向删除） */
    void deleteFriend(Long currentUserId, Long userId);

    /** 更新最后一条消息（发消息时调用） */
    void updateLastMessage(Long myself, Long friend, String content, Boolean isRead);

    // 获取当前用户的好友申请列表
    List<FriendVo> getFriendRequests(Long currentUserId);

    // 同意好友申请
    void acceptFriendRequest(Long requestId, Long currentUserId);

    // 删除/拒绝好友申请
    void deleteFriendRequest(Long requestId, Long currentUserId);

    /** 获取某人的聊天记录（分页） */
    List<FriendVo> getMessages(Long myId, Long friendId, Integer page, Integer pageSize);

    /** 标记和某人的消息已读 */
    void markRead(Long myId, Long friendId);

    //发送消息
    int insertMessage(Friend friend);

}
