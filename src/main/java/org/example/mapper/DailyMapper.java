package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.example.entity.Daily;
import org.apache.ibatis.annotations.Mapper;

//数据库数据访问接口
@Mapper
public interface DailyMapper extends BaseMapper<Daily> {
    // 此处只放自定义的 SQL 方法（如果有）
    // 🔥 原生 SQL 执行物理删除，绕过 @TableLogic
    @Delete("DELETE FROM daily WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);
}
