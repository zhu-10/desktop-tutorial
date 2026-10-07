package org.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.jsonwebtoken.io.IOException;
import org.example.entity.Daily;
import org.example.entity.User;
import org.example.exception.BizException;
import org.example.mapper.DailyMapper;
import org.example.mapper.UserMapper;
import org.example.service.DailyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;

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
    //检查用户，上传数据
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
        //设置是否有图片
        if (daily.getImage() != null && !daily.getImage().isEmpty()) {
            daily.setType(1);  // 有图
        } else {
            daily.setType(0);  // 纯文本
        }
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

    @Override
    public String uploadDailyImage(MultipartFile file, Long currentUserId) {
        // 1. 校验用户
        if (currentUserId == null) {
            throw new BizException("未登录");
        }
        User user = UserMapper.selectById(currentUserId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        // 2. 校验文件非空
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }

        // 3. 校验类型
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }
        if (!ext.matches("\\.(jpg|jpeg|png|gif|webp|bmp)")) {
            throw new BizException("只允许上传图片文件");
        }

        // 4. 校验大小
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BizException("图片大小不能超过 5MB");
        }

        // 5. 生成新文件名
        String newFileName = UUID.randomUUID().toString().replace("-", "") + ext;

        // 6. 保存目录
        String baseDir = System.getProperty("user.dir") + "/uploads/images/";
        File dir = new File(baseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File dest = new File(dir, newFileName);

        // 7. 一层 try-catch 就够了
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BizException("图片保存失败：" + e.getMessage(), e);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }

        return "/uploads/images/" + newFileName;
    }
}