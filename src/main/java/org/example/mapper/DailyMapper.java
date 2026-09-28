package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.entity.Daily;
import org.apache.ibatis.annotations.Mapper;

//数据库数据访问接口
@Mapper
public interface DailyMapper extends BaseMapper<Daily> {
    // 此处只放自定义的 SQL 方法（如果有）
}
