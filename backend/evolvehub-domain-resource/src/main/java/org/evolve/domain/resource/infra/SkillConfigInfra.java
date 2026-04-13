package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.SkillConfigEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 技能配置数据访问层
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/13
 */
@Repository
public class SkillConfigInfra extends ServiceImpl<SkillConfigInfra.SkillConfigMapper, SkillConfigEntity> {

    @Mapper
    interface SkillConfigMapper extends BaseMapper<SkillConfigEntity> {}

    public SkillConfigEntity getSkillConfigById(Long id) {
        return this.getById(id);
    }

    public SkillConfigEntity getByName(String name) {
        return this.lambdaQuery().eq(SkillConfigEntity::getName, name).one();
    }

    public void createSkillConfig(SkillConfigEntity entity) {
        this.save(entity);
    }

    public void updateSkillConfig(SkillConfigEntity entity) {
        this.updateById(entity);
    }

    public void deleteSkillConfig(Long id) {
        this.removeById(id);
    }

    /**
     * 按 scope 分页查询
     */
    public Page<SkillConfigEntity> listPageByScope(String scope, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(SkillConfigEntity::getScope, scope)
                .page(new Page<>(pageNum, pageSize));
    }

    /**
     * 按 ownerId 分页查询（用户级资源）
     */
    public Page<SkillConfigEntity> listPageByOwnerId(Long ownerId, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(SkillConfigEntity::getScope, "USER")
                .eq(SkillConfigEntity::getOwnerId, ownerId)
                .page(new Page<>(pageNum, pageSize));
    }

    /**
     * 查询用户可用的系统级技能（通过授权 ID 列表）
     */
    public List<SkillConfigEntity> listByIdsAndScope(List<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return List.of();
        }
        return this.lambdaQuery()
                .eq(SkillConfigEntity::getScope, "SYSTEM")
                .eq(SkillConfigEntity::getEnabled, 1)
                .in(SkillConfigEntity::getId, resourceIds)
                .list();
    }
}
