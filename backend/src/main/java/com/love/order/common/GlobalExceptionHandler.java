package com.love.order.common;

import cn.dev33.satoken.exception.NotLoginException;
import com.love.order.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public R<?> handleNotLogin(NotLoginException e) {
        return R.fail(401, "请先登录");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public R<?> handleIllegalArgument(IllegalArgumentException e) {
        return R.fail(400, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R<?> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail("服务器繁忙，请稍后再试");
    }
}
