package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.response.FileGuardConfigResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.FileGuardConfigInfra;
import org.evolve.domain.resource.model.FileGuardConfigEntity;
import org.springframework.stereotype.Service;

/**
 * 获取文件守卫配置
 */
@Service
public class FileGuardConfigGetManager extends BaseManager<Void, FileGuardConfigResponse> {

    @Resource
    private FileGuardConfigInfra fileGuardConfigInfra;

    @Override
    protected void check(Void request) {}

    @Override
    protected FileGuardConfigResponse process(Void request) {
        FileGuardConfigEntity config = fileGuardConfigInfra.getConfig();
        if (config == null) {
            // 返回默认值
            return new FileGuardConfigResponse(1, null);
        }
        return new FileGuardConfigResponse(
                config.getEnabled(),
                config.getUpdateTime() != null ? config.getUpdateTime().toString() : null
        );
    }
}
