package org.evolve.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.SkillConfigInfra;
import org.evolve.domain.resource.model.SkillConfigEntity;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.springframework.stereotype.Service;

/**
 * 分页查询系统级技能配置列表业务处理器
 *
 * @author zhao
 */
@Service
public class ListSkillConfigManager extends BaseManager<PageRequest, PageResponse<SkillConfigEntity>> {

    @Resource
    private SkillConfigInfra skillConfigInfra;

    @Override
    protected void check(PageRequest request) {
    }

    @Override
    protected PageResponse<SkillConfigEntity> process(PageRequest request) {
        Page<SkillConfigEntity> page = skillConfigInfra.listPageByScope("SYSTEM", request.pageNum(), request.pageSize());
        return new PageResponse<>(page.getRecords(), page.getTotal(), request.pageNum(), request.pageSize());
    }
}
