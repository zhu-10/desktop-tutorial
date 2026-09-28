package org.example.controller;

import org.example.entity.Daily;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.example.service.DailyService;
import org.example.utils.JwtUtil;
import org.example.utils.Result;
import org.example.utils.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/shu")
public class DailyController {

    @Autowired
    private DailyService dailyService;

    @Autowired
    private UserMapper userMapper;// 新增

    @Autowired
    private JwtUtil jwtUtil;


    // ========== 新增 ==========
    @PostMapping("/daily")
    public Result<Long> add(@RequestPart("theme") String theme,
                            @RequestPart("content") String content,
                            @RequestPart(value = "image", required = false) MultipartFile image,
                            HttpServletRequest request) {
        // 1. 解析 Token 获取当前用户ID
        String userIdStr = jwtUtil.getUserIdFromRequest(request);
        if (userIdStr == null || userIdStr.isEmpty()) {
            return Result.error("未登录或Token无效");
        }
        Long currentUserId;
        try {
            currentUserId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            return Result.error("用户ID格式错误");
        }

        // 2. 查询用户真实姓名
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 3. 构建 Daily 对象
        Daily daily = new Daily();
        daily.setUserId(currentUserId);              // 存储用户ID（外键）
        daily.setUsername(user.getUsername());        // 存储用户名字（冗余显示字段）
        daily.setTheme(theme);
        daily.setContent(content);
        daily.setIsPublic(1);                         // 默认公开，前端可传
        daily.setCreateTime(LocalDateTime.now());
        daily.setUpdateTime(LocalDateTime.now());

        // 4. 处理图片（如果有）
        if (image != null && !image.isEmpty()) {
            try {
                String imagePath = FileUploadUtil.saveImage(image);
                daily.setImage(imagePath);
            } catch (IOException e) {
                return Result.error("图片保存失败：" + e.getMessage());
            }
        }

        // 5. 保存
        boolean saved = dailyService.save(daily);
        return saved ? Result.<Long>success("发布成功", daily.getId()) : Result.error("发布失败");
    }

    // ========== 删除 ==========
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        // 权限校验：只能删除自己的
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) return Result.error("未登录");

        Daily daily = dailyService.getById(id);
        if (daily == null) return Result.error("动态不存在");
        if (!Objects.equals(daily.getUserId(), currentUserId)) {
            return Result.error("无权删除别人的动态");
        }

        boolean removed = dailyService.removeById(id);
        return removed ? Result.success("删除成功") : Result.error("删除失败");
    }

    // ========== 修改 ==========
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Daily daily, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) return Result.error("未登录");

        Daily exist = dailyService.getById(id);
        if (exist == null) return Result.error("动态不存在");
        if (!exist.getUserId().equals(currentUserId)) {
            return Result.error("无权修改别人的动态");
        }

        // 只允许修改 content、theme、isPublic、image（由前端传入）
        exist.setTheme(daily.getTheme());
        exist.setContent(daily.getContent());
        exist.setIsPublic(daily.getIsPublic());
        if (daily.getImage() != null) {
            exist.setImage(daily.getImage());
        }
        exist.setUpdateTime(LocalDateTime.now());

        boolean updated = dailyService.updateById(exist);
        // 修改
        if (updated) {
            return Result.success("修改成功");
        } else {
            return Result.error("修改失败");
        }
    }


    // ========== 分页查询列表 ==========
    @GetMapping("/list")
    //方法返回值
    public Result<List<Daily>> getDailyList(
            //分页参数  @RequestParam：Spring MVC 注解，表示这个参数从 HTTP 请求参数里取。
            @RequestParam(defaultValue = "1") Integer page, //defaultValue = "1"：如果前端不传 page，默认第 1 页。
            @RequestParam(defaultValue = "10") Integer pageSize,    //defaultValue = "10"：如果前端不传 pageSize，默认每页 10 条。
            @RequestParam(required = false) Integer isPublic,   //isPublic：是否公开。常见约定是 1 公开，0 私有，null 表示不限制
            @RequestParam(required = false) Long userId,//userId：要查询哪个用户的日报。
            HttpServletRequest request) {   //请求对象

        Long currentUserId = getCurrentUserId(request);  // 获取当前登录用户ID

        //查询当前页数据
        List<Daily> list = dailyService.listByCondition(page, pageSize, isPublic, userId, currentUserId);
        //查询总条数
        Long total = dailyService.countByCondition(isPublic, userId, currentUserId);
        //返回统一结果
        return Result.success(list, total);
    }

    // ========== 工具方法：从Request中提取当前用户ID ==========
    private Long getCurrentUserId(HttpServletRequest request) {
        String userIdStr = jwtUtil.getUserIdFromRequest(request);
        if (userIdStr == null || userIdStr.isEmpty()) return null;
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    //查询个人数据
    @GetMapping("/my")
    public Result<List<Daily>> getMyDaily(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        //检查用户是否登录
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) return Result.error("未登录");


        // 校验用户是否存在，不存在会抛异常，由全局异常处理器统一返回
        List<Daily> list = dailyService.listByCondition(page, pageSize, null, currentUserId, currentUserId);
        Long total = dailyService.countByCondition(null, currentUserId, currentUserId);
        return Result.success(list, total);
    }

    //搜索路由
    @GetMapping("/search")
    public Result search(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer isPublic,//可选筛选公开数据还是私有数据
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {

        // 假设 currentUserId 从 Token 或 Session 获取
        Long currentUserId = getCurrentUserId(request);

        List<Daily> list = dailyService.searchDaily(page, pageSize, keyword, isPublic, userId, currentUserId);
        Long total = dailyService.countSearchDaily(keyword, isPublic, userId, currentUserId);

        // 封装分页结果返回
        Map<String, Object> data = new HashMap<>();
        data.put("records", list);
        data.put("total", total);
        return Result.success(data);
    }
}