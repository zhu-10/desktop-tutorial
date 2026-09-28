package org.example.utils;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class FileUploadUtil {

    // 私有构造方法，防止 new 工具类
    private FileUploadUtil() {
    }

    //检查文件，前端文件上传必填
    public static String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("文件不能为空");
        }
        //  获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IOException("文件名无效");
        }
        //截取文件后缀，例如 .jpg .png
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

        // 只允许图片后缀
        List<String> allowSuffix = Arrays.asList(".jpg", ".jpeg", ".png");
        if (!allowSuffix.contains(suffix)) {
            throw new IOException("只支持图片格式");
        }
        // 3. 用 UUID 生成新文件名，避免重名
        String newFileName = UUID.randomUUID() + suffix;

        // 建议使用配置的上传目录
        String baseDir = System.getProperty("user.dir") + "/uploads/images/";
        File dir = new File(baseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File dest = new File(dir, newFileName);
        file.transferTo(dest);

        // 返回相对路径，方便前端访问
        return "/uploads/images/" + newFileName;
    }
}