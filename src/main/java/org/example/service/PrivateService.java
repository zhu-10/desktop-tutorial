package org.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.Private;
import org.example.vo.PrivateVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

//继承IService不用手动写增删改查
//<Private>告诉MyBatis-Plus对应的表
public interface PrivateService extends IService<Private> {

    // 按发送者/接收者/已读状态分页查询
    IPage<PrivateVo> listByCondition(Integer page, Integer pageSize,
                                     Long sender, Long receiver, Integer isRead,
                                     Long currentUserId);

    Long countByCondition(Long sender, Long receiver, Integer isRead, Long currentUserId);

    void sendPrivateMessage(Long senderId, Long receiverId, String content);
    //保存私信，设置/校验当前用户
    boolean savePrivate(Private privateMessage, Long currentUserId);
    //获取用户名并配置分页
    List<PrivateVo> getPrivateMessages(String currentUsername,
                                       Integer page, Integer pageSize);
    //配置已读
    void markAsRead(Long senderId, Long currentUserId);


}