package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.SkillConfigInfra;
import org.evolve.domain.resource.model.SkillConfigEntity;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 查询单个技能配置业务处理器
 *
 * @author zhao
 */
@Service
public class GetSkillConfigManager extends BaseManager<Long, SkillConfigEntity> {

    @Resource
    private SkillConfigInfra skillConfigInfra;

    @Override
    protected void check(Long id) {
        if (skillConfigInfra.getSkillConfigById(id) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "技能配置不存在");
        }
    }

    @Override
    protected SkillConfigEntity process(Long id) {
        return skillConfigInfra.getSkillConfigById(id);
    }
}
