package top.aiolife.core.exception;

import cn.dev33.satoken.exception.NotLoginException;
import top.aiolife.sso.exception.ApiKeyAccessDeniedException;
import top.aiolife.core.constant.ResponseCodeConst;
import top.aiolife.core.resq.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，统一转换通用异常、参数校验异常和认证授权异常。
 *
 * @author Ethan
 * @date 2026-07-14
 */
@Slf4j
@RestControllerAdvice
public class ExceptionHandle {

    /**
     * 处理未被更精确规则捕获的通用异常。
     *
     * @param e 待处理的通用异常
     * @return 统一错误响应，result 为异常消息
     *
     * @author Ethan
     * @date 2026-07-14
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Object> handleException(Exception e) {
        log.error("发生异常：{}", e.getMessage(), e);
        return ApiResponse.error(ResponseCodeConst.RSCODE_COMMON_FAIL, e.getMessage());
    }

    /**
     * 处理请求参数校验失败异常。
     *
     * @param e Spring 方法参数校验异常
     * @return 统一错误响应，result 为首个字段校验消息
     *
     * @author Ethan
     * @date 2026-07-14
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        log.error("参数校验异常：{}", message);
        return ApiResponse.error(ResponseCodeConst.RECODE_PARAM_FAIL, message);
    }


    /**
     * 处理未登录或登录已失效异常。
     *
     * @param ex Sa-Token 未登录异常
     * @return HTTP 401 响应，用于前端返回登录页
     *
     * @author Ethan
     * @date 2026-07-14
     */
    @ExceptionHandler({NotLoginException.class})
    public ResponseEntity<String> handleUnauthorizedException(NotLoginException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("未授权: " + ex.getMessage());
    }

    /**
     * 处理有效 API Key 访问 MCP 协议端点之外资源的异常。
     *
     * @param ex API Key 访问范围异常
     * @return HTTP 403 响应，响应体仅包含固定访问范围提示
     *
     * @author Ethan
     * @date 2026-07-14
     */
    @ExceptionHandler(ApiKeyAccessDeniedException.class)
    public ResponseEntity<String> handleApiKeyAccessDeniedException(ApiKeyAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}
