package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper // 让Spring识别这是数据库操作层
public interface UserMapper extends BaseMapper<User> {
    // BaseMapper 自带 insert、selectById 等方法，不用自己写SQL

    // 但按用户名查询需要自定义（因为BaseMapper没有按username查的）
    @Select("SELECT * FROM demo.user WHERE username = #{username}")
    User selectByUsername(String username);
    @Select("SELECT id FROM user WHERE username = #{username}")
    Long selectIdByUsername(@Param("username") String username);

    /** 用户ID → 用户名 */
    @Select("SELECT username FROM user WHERE id = #{id}")
    String selectUsernameById(@Param("id") Long id);

    @Select("SELECT * FROM user WHERE phone = #{phone}")
    User selectByPhone(String phone);
}