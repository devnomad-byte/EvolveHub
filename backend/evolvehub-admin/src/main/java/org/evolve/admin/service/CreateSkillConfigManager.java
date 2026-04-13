package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.CreateSkillConfigRequest;
import org.evolve.admin.response.CreateSkillConfigResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.SkillConfigInfra;
import org.evolve.common.model.SkillConfigEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 创建系统级技能配置业务处理器
 *
 * @author zhao
 */
@Service
public class CreateSkillConfigManager extends BaseManager<CreateSkillConfigRequest, CreateSkillConfigResponse> {

    @Resource
    private SkillConfigInfra skillConfigInfra;

    @Override
    protected void check(CreateSkillConfigRequest request) {
        if (skillConfigInfra.getByName(request.name()) != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "技能名称已存在");
        }
    }

    @Override
    protected CreateSkillConfigResponse process(CreateSkillConfigRequest request) {
        SkillConfigEntity entity = new SkillConfigEntity();
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setSkillType(request.skillType());
        entity.setConfig(request.config());
        entity.setEnabled(request.enabled());
        entity.setScope("SYSTEM");
        entity.setOwnerId(null);
        skillConfigInfra.createSkillConfig(entity);
        return new CreateSkillConfigResponse(entity.getId());
    }
}
