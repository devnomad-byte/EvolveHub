package org.evolve.common.web.exception;

/**
 * 安全扫描异常
 * <p>
 * 在 Service 层扫描失败时抛出，由全局异常处理器捕获并返回扫描结果。
 * 使用 Object 类型避免 common 模块对 admin 模块的依赖。
 * </p>
 */
public class SecurityScanException extends RuntimeException {

    private final Object scanData;

    public SecurityScanException(Object scanData) {
        super("安全扫描未通过");
        this.scanData = scanData;
    }

    public Object getScanData() {
        return scanData;
    }
}
