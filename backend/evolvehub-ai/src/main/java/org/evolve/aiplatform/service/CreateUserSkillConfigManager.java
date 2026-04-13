package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.aiplatform.request.CreateUserSkillConfigRequest;
import org.evolve.aiplatform.response.CreateUserSkillConfigResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.SkillConfigInfra;
import org.evolve.domain.resource.model.SkillConfigEntity;
import org.springframework.stereotype.Service;

/**
 * 创建用户级技能配置业务处理器
 *
 * @author zhao
 */
@Service
public class CreateUserSkillConfigManager extends BaseManager<CreateUserSkillConfigRequest, CreateUserSkillConfigResponse> {

    @Resource
    private SkillConfigInfra skillConfigInfra;

    @Override
    protected void check(CreateUserSkillConfigRequest request) {
    }

    @Override
    protected CreateUserSkillConfigResponse process(CreateUserSkillConfigRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        SkillConfigEntity entity = new SkillConfigEntity();
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setSkillType(request.skillType());
        entity.setConfig(request.config());
        entity.setEnabled(request.enabled());
        entity.setScope("USER");
        entity.setOwnerId(currentUserId);
        skillConfigInfra.createSkillConfig(entity);
        return new CreateUserSkillConfigResponse(entity.getId());
    }
}
