package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.request.UpdateFileGuardConfigRequest;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.FileGuardConfigInfra;
import org.evolve.domain.resource.model.FileGuardConfigEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 更新文件守卫配置
 */
@Service
public class FileGuardConfigUpdateManager extends BaseManager<UpdateFileGuardConfigRequest, Void> {

    @Resource
    private FileGuardConfigInfra fileGuardConfigInfra;

    @Override
    protected void check(UpdateFileGuardConfigRequest request) {}

    @Override
    protected Void process(UpdateFileGuardConfigRequest request) {
        FileGuardConfigEntity config = fileGuardConfigInfra.getConfig();
        if (config == null) {
            config = new FileGuardConfigEntity();
            config.setId(1L);
            config.setEnabled(1);
        }

        if (request.enabled() != null) {
            config.setEnabled(request.enabled());
        }
        config.setUpdateTime(LocalDateTime.now());

        fileGuardConfigInfra.saveOrUpdate(config);
        return null;
    }
}
