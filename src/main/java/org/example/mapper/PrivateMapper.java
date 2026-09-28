package org.example.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.entity.Private;
import org.example.vo.PrivateVo;

//负责跟数据库打交道
@Mapper
//继承BaseMapper，配置基本增删改查
public interface PrivateMapper extends BaseMapper<Private> {
    //使用XML 绑定，当多表 JOIN、动态 SQL、复杂查询，大型项目、SQL 常改
    //@Param多个参数	必须写，否则只能用 param1、param2 或 arg0、arg1；使用了动态 SQL，需要按名引用必须写，否则容易出错
    //配置已读sql
    int markAsRead(@Param("senderId") Long senderId,
                   @Param("currentUserId") Long currentUserId);
    /** 分页查询当前用户参与的所有私信（带发送者用户名） */
    IPage<PrivateVo> getConversations(Page<PrivateVo> page, @Param("userId") Long userId);
    //配置获取用户和私信数据sql
    IPage<PrivateVo> selectPrivateVoPage(IPage<PrivateVo> page,
                                         @Param("currentUserId") Long currentUserId,
                                         @Param("sender") Long sender,
                                         @Param("receiver") Long receiver,
                                         @Param("isRead") Integer isRead);
}
