package org.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.Private;
import org.example.mapper.PrivateMapper;
import org.example.mapper.UserMapper;
import org.example.service.PrivateService;
import org.example.vo.PrivateVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PrivateServiceImpl extends ServiceImpl<PrivateMapper, Private> implements PrivateService {

    @Autowired
    private PrivateMapper privateMapper;

    @Autowired
    private UserMapper userMapper;

    public void sendPrivateMessage(Long senderId, Long receiverId, String content) {
        Private privateMsg = new Private();
        privateMsg.setSender(senderId);
        privateMsg.setContent(content);
        privateMsg.setReceiver(receiverId); // ✅ 核心防报错点
        privateMapper.insert(privateMsg);
    }

    /** 发送私信 */
    @Override
    public boolean savePrivate(Private privateMessage, Long currentUserId) {
        if (currentUserId == null) {
            throw new RuntimeException("未登录，无法发送私信");
        }
        privateMessage.setSender(currentUserId);
        return save(privateMessage);
    }


    /** 获取用户和私信数据 */
    @Override
    public IPage<PrivateVo> listByCondition(Integer page, Integer pageSize,
                                            Long sender, Long receiver, Integer isRead,
                                            Long currentUserId) {
        if (currentUserId == null) {
            throw new RuntimeException("请先登录");
        }
        Page<PrivateVo> pageParam = new Page<>(page, pageSize);
        return baseMapper.selectPrivateVoPage(pageParam, currentUserId, sender, receiver, isRead);
    }

    /** 统计条数 */
    @Override
    public Long countByCondition(Long sender, Long receiver, Integer isRead,
                                 Long currentUserId) {
        if (currentUserId == null) {
            throw new RuntimeException("请先登录");
        }

        LambdaQueryWrapper<Private> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Private::getSender, currentUserId)
                .or()
                .eq(Private::getReceiver, currentUserId));

        if (sender != null)   wrapper.eq(Private::getSender, sender);
        if (receiver != null) wrapper.eq(Private::getReceiver, receiver);
        if (isRead != null)   wrapper.eq(Private::getIsRead, isRead);

        return baseMapper.selectCount(wrapper);
    }

    /**
     * 获取当前用户的私信列表（带发送者用户名）
     * @param currentUsername 从前端/安全上下文拿到的"用户名或ID"
     */
    @Override
    public List<PrivateVo> getPrivateMessages(String currentUsername,
                                              Integer page, Integer pageSize) {

        // ① 拿到当前用户 ID（无论传进来的是用户名还是 ID，都统一转成 ID）
        Long currentUserId;
        if (currentUsername != null && currentUsername.matches("\\d+")) {
            // 是纯数字 → 当成 ID
            currentUserId = Long.valueOf(currentUsername);
        } else {
            // 当成用户名 → 查 user 表
            currentUserId = userMapper.selectIdByUsername(currentUsername);
        }

        if (currentUserId == null) {
            return Collections.emptyList();
        }

        // ② 用 ID 分页查询（XML 里 JOIN user 表拿用户名）
        Page<PrivateVo> pageParam = new Page<>(page, pageSize);
        IPage<PrivateVo> result = privateMapper.getConversations(pageParam, currentUserId);
        return result.getRecords();
    }

    /** 标记已读 */
    @Override
    public void markAsRead(Long senderId, Long currentUserId) {
        privateMapper.markAsRead(senderId, currentUserId);
    }
}