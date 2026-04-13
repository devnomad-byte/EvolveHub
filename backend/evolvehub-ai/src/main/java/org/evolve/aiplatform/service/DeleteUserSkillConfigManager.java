package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.SkillConfigInfra;
import org.evolve.domain.resource.model.SkillConfigEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 删除用户级技能配置业务处理器
 *
 * @author zhao
 */
@Service
public class DeleteUserSkillConfigManager extends BaseManager<Long, Void> {

    @Resource
    private SkillConfigInfra skillConfigInfra;

    @Override
    protected void check(Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SkillConfigEntity existing = skillConfigInfra.getSkillConfigById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "技能配置不存在");
        }
        if (!"USER".equals(existing.getScope()) || !currentUserId.equals(existing.getOwnerId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该技能配置");
        }
    }

    @Override
    protected Void process(Long id) {
        skillConfigInfra.deleteSkillConfig(id);
        return null;
    }
}
