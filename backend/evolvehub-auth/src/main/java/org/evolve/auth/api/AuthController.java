package org.evolve.auth.api;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.auth.request.LoginRequest;
import org.evolve.auth.response.LoginResponse;
import org.evolve.auth.service.LoginManager;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * <p>
 * 提供登录、登出接口。login 在网关白名单中，无需携带 token。
 * 用户注册功能已移除，用户只能由管理员在后台管理界面创建。
 * </p>
 *
 * @author zhao
 */
@RestController
public class AuthController {

    @Resource
    private LoginManager loginManager;

    /**
     * 用户登录
     *
     * @param request 登录请求体（username + password）
     * @return token 及用户信息
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return Result.ok(loginManager.execute(request));
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.ok();
    }
}
