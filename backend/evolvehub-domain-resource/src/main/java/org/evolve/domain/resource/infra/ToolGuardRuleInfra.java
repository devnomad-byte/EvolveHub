package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.ToolGuardRuleEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 工具守卫规则数据访问层
 */
@Repository
public class ToolGuardRuleInfra extends ServiceImpl<ToolGuardRuleInfra.ToolGuardRuleMapper, ToolGuardRuleEntity> {

    @Mapper
    public interface ToolGuardRuleMapper extends BaseMapper<ToolGuardRuleEntity> {}

    public ToolGuardRuleEntity getById(Long id) {
        return this.lambdaQuery()
                .eq(ToolGuardRuleEntity::getDeleted, 0)
                .eq(ToolGuardRuleEntity::getId, id)
                .one();
    }

    public ToolGuardRuleEntity getByRuleId(String ruleId) {
        return this.lambdaQuery()
                .eq(ToolGuardRuleEntity::getDeleted, 0)
                .eq(ToolGuardRuleEntity::getRuleId, ruleId)
                .one();
    }

    public List<ToolGuardRuleEntity> getEnabledRules() {
        return this.lambdaQuery()
                .eq(ToolGuardRuleEntity::getDeleted, 0)
                .eq(ToolGuardRuleEntity::getEnabled, 1)
                .list();
    }

    public List<ToolGuardRuleEntity> listAll() {
        return this.lambdaQuery()
                .eq(ToolGuardRuleEntity::getDeleted, 0)
                .orderByAsc(ToolGuardRuleEntity::getSeverity)
                .orderByAsc(ToolGuardRuleEntity::getRuleId)
                .list();
    }
}
