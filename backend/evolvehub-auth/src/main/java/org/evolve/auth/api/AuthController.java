package org.evolve.auth.api;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.auth.request.LoginRequest;
import org.evolve.auth.request.RegisterRequest;
import org.evolve.auth.response.LoginResponse;
import org.evolve.auth.response.RegisterResponse;
import org.evolve.auth.service.LoginManager;
import org.evolve.auth.service.RegisterManager;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * <p>
 * 提供登录、注册、登出接口。login/register 在网关白名单中，无需携带 token。
 * </p>
 *
 * @author zhao
 */
@RestController
public class AuthController {

    @Resource
    private LoginManager loginManager;

    @Resource
    private RegisterManager registerManager;

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
     * 用户注册
     *
     * @param request 注册请求体
     * @return 新用户 ID 和用户名
     */
    @PostMapping("/register")
    public Result<RegisterResponse> register(@RequestBody @Valid RegisterRequest request) {
        return Result.ok(registerManager.execute(request));
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
