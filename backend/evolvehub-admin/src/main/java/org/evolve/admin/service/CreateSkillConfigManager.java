package org.evolve.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.admin.request.CreateSkillConfigRequest;
import org.evolve.admin.response.CreateSkillConfigResponse;
import org.evolve.admin.response.SecurityScanResult;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.SkillConfigInfra;
import org.evolve.domain.resource.model.SkillConfigEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.exception.SecurityScanException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 创建技能配置业务处理器
 *
 * @author zhao
 */
@Service
public class CreateSkillConfigManager extends BaseManager<CreateSkillConfigRequest, CreateSkillConfigResponse> {

    @Resource
    private SkillConfigInfra skillConfigInfra;

    @Resource
    private SecurityScanner securityScanner;

    @Override
    protected void check(CreateSkillConfigRequest request) {
        if (skillConfigInfra.getByName(request.name()) != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "技能名称已存在");
        }
        // DEPT scope 时 deptId 不能为空
        if ("DEPT".equals(request.scope()) && request.deptId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "部门级 Skill 必须指定部门");
        }
        // 安全扫描 SKILL.md 内容
        if (request.content() != null && !request.content().isEmpty()) {
            SecurityScanResult scanResult = securityScanner.scanText(request.content(), "SKILL.md");
            if (!scanResult.isPassed()) {
                throw new SecurityScanException(scanResult);
            }
        }
    }

    @Override
    protected CreateSkillConfigResponse process(CreateSkillConfigRequest request) {
        SkillConfigEntity entity = new SkillConfigEntity();
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setSkillType(request.skillType());
        entity.setContent(request.content());
        entity.setSource(request.source() != null ? request.source() : "MANUAL");
        entity.setSourceUrl(request.sourceUrl());
        entity.setTags(request.tags());
        entity.setConfig(request.config());
        entity.setEnabled(request.enabled());
        entity.setScope(request.scope() != null ? request.scope() : "SYSTEM");
        entity.setDeptId(request.deptId());
        // USER scope 时 ownerId 通过 SecurityContext 获取
        entity.setOwnerId("USER".equals(request.scope()) ? getCurrentUserId() : null);
        skillConfigInfra.createSkillConfig(entity);
        return new CreateSkillConfigResponse(entity.getId());
    }

    private Long getCurrentUserId() {
        return StpUtil.getLoginIdAsLong();
    }
}
