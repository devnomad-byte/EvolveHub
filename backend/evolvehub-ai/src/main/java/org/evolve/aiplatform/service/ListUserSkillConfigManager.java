package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.infra.SkillConfigInfra;
import org.evolve.domain.resource.model.SkillConfigEntity;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.springframework.stereotype.Service;

/**
 * 分页查询当前用户的用户级技能配置列表
 *
 * @author zhao
 */
@Service
public class ListUserSkillConfigManager extends BaseManager<PageRequest, PageResponse<SkillConfigEntity>> {

    @Resource
    private SkillConfigInfra skillConfigInfra;

    @Override
    protected void check(PageRequest request) {
    }

    @Override
    protected PageResponse<SkillConfigEntity> process(PageRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        Page<SkillConfigEntity> page = skillConfigInfra.listPageByOwnerId(currentUserId, request.pageNum(), request.pageSize());
        return new PageResponse<>(page.getRecords(), page.getTotal(), request.pageNum(), request.pageSize());
    }
}
