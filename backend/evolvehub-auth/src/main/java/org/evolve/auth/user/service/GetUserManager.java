package org.evolve.auth.user.service;

import jakarta.annotation.Resource;
import org.evolve.auth.user.infra.UsersInfra;
import org.evolve.auth.user.model.UsersEntity;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 查询用户详情业务处理器
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Service
public class GetUserManager extends BaseManager<Long, UsersEntity> {

    @Resource
    private UsersInfra usersInfra;

    @Override
    protected void check(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户ID不能为空");
        }
    }

    @Override
    protected UsersEntity process(Long id) {
        UsersEntity user = usersInfra.getUserById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "用户不存在");
        }
        return user;
    }
}
