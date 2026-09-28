package org.example.utils;

import lombok.Data;


//配置统一接口返回格式的工具类
@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;
    private Long total;   // 分页总记录数

    // 🚀 新增：无数据的成功返回（专供 add、delete、update 使用）
    public static <T> Result<T> success() {
        return success(null);
    }
    // 请求成功，并返回业务数据，状态码固定为 200
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    //表示请求成功，并可以自定义提示信息
    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("成功");
        result.setData(data);
        return result;
    }

    // 新增：支持分页数据的成功返回总记录数 total
    public static <T> Result<T> success(T data, Long total) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("成功");
        result.setData(data);
        result.setTotal(total);
        return result;
    }

    // 表示请求失败，状态码固定为 500，并返回错误提示
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg("系统异常");
        return result;
    }
}