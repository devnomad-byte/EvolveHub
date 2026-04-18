package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.ToolGuardHistoryInfra;
import org.springframework.stereotype.Service;

/**
 * 清除工具守卫历史记录
 */
@Service
public class ToolGuardHistoryDeleteManager extends BaseManager<Void, Void> {

    @Resource
    private ToolGuardHistoryInfra toolGuardHistoryInfra;

    @Override
    protected void check(Void request) {}

    @Override
    protected Void process(Void request) {
        toolGuardHistoryInfra.clearAll();
        return null;
    }
}
