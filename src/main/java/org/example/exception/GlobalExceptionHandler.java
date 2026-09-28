package org.example.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.utils.Result;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;


//全局异常处理器
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 1. 业务异常：直接返回真实提示信息
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /**
     * 2. @RequestBody + @Valid 校验失败
     *    （MethodArgumentNotValidException）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = (fieldError != null)
                ? fieldError.getDefaultMessage()
                : "参数校验失败";
        log.warn("参数校验异常: {}", msg);
        return Result.error(msg);
    }

    /**
     * 3. @RequestParam / @PathVariable + @Validated 校验失败
     *    （ConstraintViolationException）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("参数校验失败");
        log.warn("参数校验异常: {}", msg);
        return Result.error(msg);
    }

    /**
     * 4. 参数类型不匹配，例如 ?id=abc
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String msg = "参数 [" + e.getName() + "] 类型不正确";
        log.warn("参数类型异常: {}", msg);
        return Result.error(msg);
    }

    /**
     * 5. 兜底：其他未预期异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);   // 打完整堆栈到日志，别用 e.printStackTrace()
        return Result.error("系统开小差了，请稍后再试");
    }
}