package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.evolve.domain.resource.infra.FileGuardRuleInfra;
import org.evolve.domain.resource.model.FileGuardRuleEntity;
import org.springframework.stereotype.Service;

/**
 * 删除文件守卫规则
 */
@Service
public class FileGuardRuleDeleteManager extends BaseManager<Long, Void> {

    @Resource
    private FileGuardRuleInfra fileGuardRuleInfra;

    @Override
    protected void check(Long id) {
        FileGuardRuleEntity existing = fileGuardRuleInfra.getById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "规则不存在: " + id);
        }
        // 内置规则不允许删除
        if (existing.getIsBuiltin() == 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "内置规则不允许删除");
        }
    }

    @Override
    protected Void process(Long id) {
        // 逻辑删除
        FileGuardRuleEntity entity = fileGuardRuleInfra.getById(id);
        entity.setDeleted(1);
        fileGuardRuleInfra.updateById(entity);
        return null;
    }
}
