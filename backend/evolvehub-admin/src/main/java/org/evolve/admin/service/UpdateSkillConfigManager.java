package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.admin.request.UpdateSkillConfigRequest;
import org.evolve.admin.response.UpdateSkillConfigResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.SkillConfigInfra;
import org.evolve.domain.resource.model.SkillConfigEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 更新技能配置业务处理器
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
        // DEPT scope 时 deptId 不能为空
        if (request.scope() != null && "DEPT".equals(request.scope()) && request.deptId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "部门级 Skill 必须指定部门");
        }
    }

    @Override
    protected UpdateSkillConfigResponse process(UpdateSkillConfigRequest request) {
        SkillConfigEntity entity = new SkillConfigEntity();
        entity.setId(request.id());
        if (request.name() != null) entity.setName(request.name());
        if (request.description() != null) entity.setDescription(request.description());
        if (request.skillType() != null) entity.setSkillType(request.skillType());
        if (request.content() != null) entity.setContent(request.content());
        if (request.source() != null) entity.setSource(request.source());
        if (request.sourceUrl() != null) entity.setSourceUrl(request.sourceUrl());
        if (request.tags() != null) entity.setTags(request.tags());
        if (request.config() != null) entity.setConfig(request.config());
        if (request.enabled() != null) entity.setEnabled(request.enabled());
        if (request.scope() != null) entity.setScope(request.scope());
        if (request.deptId() != null) entity.setDeptId(request.deptId());
        // scope 变更时同步更新 ownerId
        if (request.scope() != null) {
            if ("USER".equals(request.scope())) {
                entity.setOwnerId(StpUtil.getLoginIdAsLong());
            } else {
                entity.setOwnerId(null);
            }
        }
        skillConfigInfra.updateSkillConfig(entity);
        return new UpdateSkillConfigResponse(request.id());
    }
}
