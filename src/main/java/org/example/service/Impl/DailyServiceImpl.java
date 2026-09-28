package org.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.Daily;
import org.example.entity.User;
import org.example.mapper.DailyMapper;
import org.example.mapper.UserMapper;
import org.example.service.DailyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyServiceImpl extends ServiceImpl<DailyMapper, Daily> implements DailyService {
    //Autowired自动注入依赖对象
    @Autowired
    private UserMapper UserMapper;

    // 抽取公共的搜索条件拼接逻辑
    private void appendSearchCondition(LambdaQueryWrapper<Daily> wrapper, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.likeRight(Daily::getUsername, keyword)
                    .or()
                    .likeRight(Daily::getTheme, keyword)
                    .or()
                    .likeRight(Daily::getContent, keyword));
        }
    }

    // 保存 Daily 的方法
    //检查用户
    @Override
    public boolean saveDaily(Daily daily, Long currentUserId) {
        // 检查用户
        User user = UserMapper.selectById(currentUserId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 设置日报的作者ID
        daily.setUserId(currentUserId);
        //设置日报的作者用户名
        daily.setUsername(user.getUsername());
        //保存数据
        return save(daily);
    }

    //实现 Service 接口里定义的方法
    //获取数据并显示数据
    @Override
    public List<Daily> listByCondition(Integer page, Integer pageSize, Integer isPublic, Long userId, Long currentUserId) {
        LambdaQueryWrapper<Daily> wrapper = new LambdaQueryWrapper<>();

        // 1. 如果传了 userId → 个人主页，查询该用户的全部文章（含隐藏）
        if (userId != null) {
            wrapper.eq(Daily::getUserId, userId);
        } else {
            // 2. 没传 userId → 首页，默认只查公开文章
            wrapper.eq(Daily::getIsPublic, 1);
        }

        // 3. 如果前端明确传了 isPublic，则按前端的要求过滤
        if (isPublic != null) {
            wrapper.eq(Daily::getIsPublic, isPublic);
        }

        // 4. 按发布时间倒序
        wrapper.orderByDesc(Daily::getCreateTime);

        Page<Daily> pageParam = new Page<>(page, pageSize);
        IPage<Daily> result = baseMapper.selectPage(pageParam, wrapper);
        return result.getRecords();
    }

    //统计主页数据总数
    @Override
    public Long countByCondition(Integer isPublic, Long userId, Long currentUserId) {
        LambdaQueryWrapper<Daily> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(Daily::getUserId, userId);
        } else {
            wrapper.eq(Daily::getIsPublic, 1);
        }

        if (isPublic != null) {
            wrapper.eq(Daily::getIsPublic, isPublic);
        }

        return baseMapper.selectCount(wrapper);
    }

    //实现搜索功能
    @Override
    public List<Daily> searchDaily(Integer page, Integer pageSize, String keyword, Integer isPublic, Long userId, Long currentUserId) {
        LambdaQueryWrapper<Daily> wrapper = new LambdaQueryWrapper<>();

        // 权限与归属过滤
        if (userId != null) {
            wrapper.eq(Daily::getUserId, userId);
        } else {
            wrapper.eq(Daily::getIsPublic, 1);
        }
        if (isPublic != null) {
            wrapper.eq(Daily::getIsPublic, isPublic);
        }

        // 调用抽取的搜索条件
        appendSearchCondition(wrapper, keyword);

        // 排序与分页
        wrapper.orderByDesc(Daily::getCreateTime);
        Page<Daily> pageParam = new Page<>(page, pageSize);
        return baseMapper.selectPage(pageParam, wrapper).getRecords();
    }

    //统计搜索条件的数据总数
    @Override
    public Long countSearchDaily(String keyword, Integer isPublic, Long userId, Long currentUserId) {
        LambdaQueryWrapper<Daily> wrapper = new LambdaQueryWrapper<>();

        // 权限过滤
        if (userId != null) {
            wrapper.eq(Daily::getUserId, userId);
        } else {
            wrapper.eq(Daily::getIsPublic, 1);
        }
        if (isPublic != null) {
            wrapper.eq(Daily::getIsPublic, isPublic);
        }

        // 调用抽取的搜索条件
        appendSearchCondition(wrapper, keyword);
        //统计数量
        return baseMapper.selectCount(wrapper);
    }
}