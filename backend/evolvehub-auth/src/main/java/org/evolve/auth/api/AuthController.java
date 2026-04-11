package org.evolve.auth.api;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.auth.request.InitAdminRequest;
import org.evolve.auth.request.LoginRequest;
import org.evolve.auth.response.LoginResponse;
import org.evolve.auth.service.InitAdminManager;
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
 * @author zhao devnomad-byte
 */
@RestController
public class AuthController {

    @Resource
    private LoginManager loginManager;

    @Resource
    private InitAdminManager initAdminManager;

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

    /**
     * 初始化超级管理员
     * <p>
     * 安全限制：
     * <ul>
     *     <li>只在数据库没有用户时可用</li>
     *     <li>可选：验证环境变量 INIT_SECRET_KEY</li>
     *     <li>创建成功后自动登录</li>
     * </ul>
     *
     * @param request 初始化请求（username + password + nickname + initSecretKey）
     * @return token 及用户信息
     */
    @PostMapping("/init-admin")
    public Result<LoginResponse> initAdmin(@RequestBody @Valid InitAdminRequest request) {
        return Result.ok(initAdminManager.execute(request));
    }
}
