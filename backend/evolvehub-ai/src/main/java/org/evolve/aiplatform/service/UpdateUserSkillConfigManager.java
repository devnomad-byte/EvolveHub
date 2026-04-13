package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.request.UpdateUserSkillConfigRequest;
import org.evolve.aiplatform.response.UpdateUserSkillConfigResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.SkillConfigInfra;
import org.evolve.common.model.SkillConfigEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 更新用户级技能配置业务处理器
 *
 * @author zhao
 */
@Service
public class UpdateUserSkillConfigManager extends BaseManager<UpdateUserSkillConfigRequest, UpdateUserSkillConfigResponse> {

    @Resource
    private SkillConfigInfra skillConfigInfra;

    @Override
    protected void check(UpdateUserSkillConfigRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SkillConfigEntity existing = skillConfigInfra.getSkillConfigById(request.id());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "技能配置不存在");
        }
        if (!"USER".equals(existing.getScope()) || !currentUserId.equals(existing.getOwnerId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该技能配置");
        }
    }

    @Override
    protected UpdateUserSkillConfigResponse process(UpdateUserSkillConfigRequest request) {
        SkillConfigEntity entity = new SkillConfigEntity();
        entity.setId(request.id());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setSkillType(request.skillType());
        entity.setConfig(request.config());
        entity.setEnabled(request.enabled());
        skillConfigInfra.updateSkillConfig(entity);
        return new UpdateUserSkillConfigResponse(request.id());
    }
}
