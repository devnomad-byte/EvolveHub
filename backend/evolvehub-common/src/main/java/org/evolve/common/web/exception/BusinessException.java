package org.evolve.common.web.exception;

import lombok.Getter;
import org.evolve.common.web.response.ResultCode;

import java.io.Serial;

/**
 * 业务异常
 * <p>
 * 在业务逻辑中主动抛出，由全局异常处理器统一捕获并返回给前端。
 * <p>
 * 用法示例：
 * <pre>
 *   throw new BusinessException(ResultCode.DATA_NOT_EXIST);
 *   throw new BusinessException("用户名已被占用");
 *   throw new BusinessException(ResultCode.FORBIDDEN, "该知识库不可访问");
 * </pre>
 */
@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAIL.getCode();
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.code = resultCode.getCode();
    }
}
