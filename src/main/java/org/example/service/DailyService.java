package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.Daily;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

//继承字段文件
public interface DailyService extends IService<Daily> {
    //根据条件查询日报列表，并支持分页
    List<Daily> listByCondition(Integer page, Integer pageSize, Integer isPublic, Long userId, Long currentUserId);
    //前端根据总记录数和每页条数，才能算出总页数
    Long countByCondition(Integer isPublic, Long userId, Long currentUserId);
    //保存一条新的数据，save业务新增
    //Daily daily：要新增的日报对象，里面可能包含标题、内容、公开状态等。
    //Long currentUserId：当前登录用户 ID。
    boolean saveDaily(Daily daily, Long currentUserId);  // 新增
    //关键字搜索日报，并分页
    //page、pageSize：分页参数。keyword：搜索关键词，可能匹配日报标题、内容等字段。
    //isPublic：公开状态过滤。userId：目标用户过滤。
    //currentUserId：当前登录用户，用于权限判断。
    List<Daily> searchDaily(Integer page, Integer pageSize, String keyword, Integer isPublic, Long userId, Long currentUserId);
    // 统计搜索结果总数
    //searchDaily：查当前页数据；countSearchDaily：查总条数。
    Long countSearchDaily(String keyword, Integer isPublic, Long userId, Long currentUserId);

    //配置上传图片方法
    String uploadDailyImage(MultipartFile file, Long currentUserId);
}

