package org.evolve.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import jakarta.annotation.Resource;
import org.evolve.auth.request.InitAdminRequest;
import org.evolve.auth.response.LoginResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.DeptInfra;
import org.evolve.common.infra.PermissionsInfra;
import org.evolve.common.infra.RolePermissionsInfra;
import org.evolve.common.infra.UserRolesInfra;
import org.evolve.common.infra.UsersInfra;
import org.evolve.common.model.DeptEntity;
import org.evolve.common.model.PermissionsEntity;
import org.evolve.common.model.RolePermissionsEntity;
import org.evolve.common.model.RolesEntity;
import org.evolve.common.model.UserRolesEntity;
import org.evolve.common.model.UsersEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 初始化超级管理员业务处理器
 * <p>
 * 安全限制：
 * <ul>
 *     <li>只在数据库没有用户时可用</li>
 *     <li>可选：验证环境变量 INIT_SECRET_KEY</li>
 *     <li>创建成功后自动登录</li>
 * </ul>
 *
 * @author zhao
 */
@Service
public class InitAdminManager extends BaseManager<InitAdminRequest, LoginResponse> {

    @Resource
    private UsersInfra usersInfra;

    @Resource
    private UserRolesInfra userRolesInfra;

    @Resource
    private RolePermissionsInfra rolePermissionsInfra;

    @Resource
    private PermissionsInfra permissionsInfra;

    @Resource
    private DeptInfra deptInfra;

    @Value("${app.init.secret-key:}")
    private String configuredSecretKey;

    @Override
    protected void check(InitAdminRequest request) {
        // 1. 检查是否已有用户（系统只能初始化一次）
        long userCount = usersInfra.count();
        if (userCount > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "系统已初始化，无法重复创建管理员");
        }

        // 2. 可选：验证初始化密钥
        if (configuredSecretKey != null && !configuredSecretKey.isBlank()) {
            if (request.initSecretKey() == null || request.initSecretKey().isBlank()) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "请提供初始化密钥");
            }
            if (!request.initSecretKey().equals(configuredSecretKey)) {
                throw new BusinessException(ResultCode.PASSWORD_ERROR, "初始化密钥错误");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    protected LoginResponse process(InitAdminRequest request) {
        // 1. 创建超级管理员用户
        UsersEntity admin = new UsersEntity();
        admin.setUsername(request.username());
        admin.setPassword(BCrypt.hashpw(request.password(), BCrypt.gensalt()));
        admin.setNickname(request.nickname());
        admin.setStatus(1); // 正常状态
        usersInfra.createUser(admin);

        // 2. 分配超级管理员角色（id=1）
        UserRolesEntity userRole = new UserRolesEntity();
        userRole.setUserId(admin.getId());
        userRole.setRoleId(1L);
        userRolesInfra.save(userRole);

        // 3. 为超级管理员分配所有权限
        List<PermissionsEntity> allPermissions = permissionsInfra.lambdaQuery()
                .eq(PermissionsEntity::getStatus, 1)
                .list();
        for (PermissionsEntity perm : allPermissions) {
            RolePermissionsEntity rolePerm = new RolePermissionsEntity();
            rolePerm.setRoleId(1L);
            rolePerm.setPermissionId(perm.getId());
            rolePermissionsInfra.save(rolePerm);
        }

        // 4. 执行登录
        StpUtil.login(admin.getId());

        return new LoginResponse(
                StpUtil.getTokenValue(),
                admin.getId(),
                admin.getUsername(),
                admin.getNickname(),
                StpUtil.getRoleList(),
                StpUtil.getPermissionList()
        );
    }
}
