package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.FileGuardRuleEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;

/**
 * 敏感文件保护规则数据访问层
 */
@Repository
public class FileGuardRuleInfra extends ServiceImpl<FileGuardRuleInfra.FileGuardRuleMapper, FileGuardRuleEntity> {

    @Mapper
    interface FileGuardRuleMapper extends BaseMapper<FileGuardRuleEntity> {}

    /**
     * 获取所有启用的规则
     */
    public List<FileGuardRuleEntity> getEnabledRules() {
        return lambdaQuery()
                .eq(FileGuardRuleEntity::getEnabled, 1)
                .eq(FileGuardRuleEntity::getDeleted, 0)
                .list();
    }

    /**
     * 根据规则ID查询
     */
    public FileGuardRuleEntity getByRuleId(String ruleId) {
        return lambdaQuery()
                .eq(FileGuardRuleEntity::getRuleId, ruleId)
                .eq(FileGuardRuleEntity::getDeleted, 0)
                .one();
    }

    /**
     * 根据ID查询
     */
    public FileGuardRuleEntity getById(Long id) {
        return lambdaQuery()
                .eq(FileGuardRuleEntity::getId, id)
                .eq(FileGuardRuleEntity::getDeleted, 0)
                .one();
    }
}
