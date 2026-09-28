package org.example.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.Friend;
import org.example.vo.FriendVo;
import org.example.vo.PrivateVo;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FriendMapper extends BaseMapper<Friend>{
    /**
     * 查某个用户的所有好友申请（按最后消息时间倒序）
     */
    List<FriendVo> selectFriendList(@Param("myself") Long myself, @Param("isRead") Boolean isRead);

    /**
     * 判断两人是否已是好友
     */
    default Long countRelation(Long myself, Long friend) {
        return selectCount(new LambdaQueryWrapper<Friend>()
                .eq(Friend::getMyself, myself)
                .eq(Friend::getFriend, friend));
    }

    /**
     * 更新最后一条消息（不新增记录）
     */
    default int updateLastMessage(Long myself, Long friend, String content, Boolean isRead) {
        return update(null, new LambdaUpdateWrapper<Friend>()
                .eq(Friend::getMyself, myself)
                .eq(Friend::getFriend, friend)
                .set(Friend::getContent, content)
                .set(Friend::getIsRead, isRead)
                .set(Friend::getUpdateTime, LocalDateTime.now()));
    }

    // 判断是否已经是「真正的好友」（双向都已同意）
    int countRealFriend(@Param("myself") Long myself,
                        @Param("friend") Long friend);

    //同意好友申请
    int acceptFriendRequest(@Param("id") Long id,
                            @Param("updateTime") LocalDateTime updateTime);


    /** 待处理申请数量 */
    int countPending(@Param("myself") Long myself,
                     @Param("friend") Long friend);

    /** 🟢 好友列表：我这边 status=1 的记录 */
    List<FriendVo> listFriends(@Param("myself") Long myself,@Param("offset")   Integer offset,
                               @Param("pageSize") Integer pageSize);

    int countRefused(@Param("myself") Long myself,   // 🟢 名字对齐
                     @Param("friend") Long friend);
    //获取好友名和聊天记录
    /** 获取我和某人的聊天记录 */
    List<FriendVo> listMessages(
            @Param("myId") Long myId,
            @Param("friendId") Long friendId,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );

    //发送消息
    int insertMessage(Friend friend);


}