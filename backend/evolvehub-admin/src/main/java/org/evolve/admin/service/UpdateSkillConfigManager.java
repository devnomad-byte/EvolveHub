package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.UpdateSkillConfigRequest;
import org.evolve.admin.response.UpdateSkillConfigResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.SkillConfigInfra;
import org.evolve.common.model.SkillConfigEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 更新系统级技能配置业务处理器
 *
 * @author zhao
 */
@Service
public class UpdateSkillConfigManager extends BaseManager<UpdateSkillConfigRequest, UpdateSkillConfigResponse> {

    @Resource
    private SkillConfigInfra skillConfigInfra;

    @Override
    protected void check(UpdateSkillConfigRequest request) {
        SkillConfigEntity existing = skillConfigInfra.getSkillConfigById(request.id());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "技能配置不存在");
        }
        if (request.name() != null && !request.name().isBlank()) {
            SkillConfigEntity byName = skillConfigInfra.getByName(request.name());
            if (byName != null && !byName.getId().equals(request.id())) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "技能名称已存在");
            }
        }
    }

    @Override
    protected UpdateSkillConfigResponse process(UpdateSkillConfigRequest request) {
        SkillConfigEntity entity = new SkillConfigEntity();
        entity.setId(request.id());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setSkillType(request.skillType());
        entity.setConfig(request.config());
        entity.setEnabled(request.enabled());
        skillConfigInfra.updateSkillConfig(entity);
        return new UpdateSkillConfigResponse(request.id());
    }
}
