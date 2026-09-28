package org.example.exception;

/**
 * 自定义业务异常
 * 继承 RuntimeException，这样 Spring 事务遇到它才会默认回滚
 */
public class BizException extends RuntimeException {

    // 无参构造
    public BizException() {
        super();
    }
    // 🚀 核心：接收 String 的构造
    public BizException(String message) {
        super(message);
    }

    // 带消息和原因
    public BizException(String message, Throwable cause) {
        super(message, cause);
    }
}