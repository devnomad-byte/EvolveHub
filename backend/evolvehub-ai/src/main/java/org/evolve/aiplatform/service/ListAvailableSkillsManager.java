package org.evolve.aiplatform.service;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.ResourceGrantInfra;
import org.evolve.common.infra.SkillConfigInfra;
import org.evolve.common.model.SkillConfigEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询当前用户可用的技能列表（自己的 USER 技能 + 被授权的 SYSTEM 技能）
 *
 * @author zhao
 */
@Service
public class ListAvailableSkillsManager extends BaseManager<Void, List<SkillConfigEntity>> {

    @Resource
    private SkillConfigInfra skillConfigInfra;

    @Resource
    private ResourceGrantInfra resourceGrantInfra;

    @Override
    protected void check(Void request) {
    }

    @Override
    protected List<SkillConfigEntity> process(Void request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        List<SkillConfigEntity> userSkills = skillConfigInfra.listPageByOwnerId(currentUserId, 1, 1000).getRecords();
        List<Long> grantedIds = resourceGrantInfra.listGrantedResourceIds(currentUserId, "SKILL");
        List<SkillConfigEntity> systemSkills = skillConfigInfra.listByIdsAndScope(grantedIds);
        List<SkillConfigEntity> result = new ArrayList<>(userSkills.size() + systemSkills.size());
        result.addAll(userSkills);
        result.addAll(systemSkills);
        return result;
    }
}
