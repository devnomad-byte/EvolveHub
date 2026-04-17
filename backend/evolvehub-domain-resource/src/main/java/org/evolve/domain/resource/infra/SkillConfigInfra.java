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
 * @version v1.1
 * @date 2026/4/14
 */
@Repository
public class SkillConfigInfra extends ServiceImpl<SkillConfigInfra.SkillConfigMapper, SkillConfigEntity> {

    @Mapper
    public interface SkillConfigMapper extends BaseMapper<SkillConfigEntity> {}

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
     * 按 scope 分页查询（SYSTEM / DEPT / USER）
     */
    public Page<SkillConfigEntity> listPageByScope(String scope, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(SkillConfigEntity::getScope, scope)
                .page(new Page<>(pageNum, pageSize));
    }

    /**
     * 按部门查询部门级技能
     */
    public Page<SkillConfigEntity> listPageByDeptId(Long deptId, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(SkillConfigEntity::getScope, "DEPT")
                .eq(SkillConfigEntity::getDeptId, deptId)
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
     * 查询用户自建的已启用技能
     *
     * @param ownerId 用户 ID
     * @return 已启用的技能配置列表
     */
    public List<SkillConfigEntity> listEnabledByOwnerId(Long ownerId) {
        return this.lambdaQuery()
                .eq(SkillConfigEntity::getScope, "USER")
                .eq(SkillConfigEntity::getOwnerId, ownerId)
                .eq(SkillConfigEntity::getEnabled, 1)
                .list();
    }

    /**
     * 查询用户可用的技能
     * <p>
     * 可见性规则同 McpConfigInfra.listVisibleMcps
     * </p>
     *
     * @param ancestorDeptIds 用户部门及其所有祖先部门的 ID 集合（含自身部门）
     * @param grantResourceIds 通过 eh_resource_grant 授权给该用户的资源 ID 列表
     */
    public List<SkillConfigEntity> listVisibleSkills(List<Long> ancestorDeptIds, List<Long> grantResourceIds) {
        return this.lambdaQuery()
                .eq(SkillConfigEntity::getEnabled, 1)
                .and(w -> {
                    w.eq(SkillConfigEntity::getScope, "SYSTEM");

                    if (ancestorDeptIds != null && !ancestorDeptIds.isEmpty()) {
                        w.or(ow -> ow
                                .eq(SkillConfigEntity::getScope, "DEPT")
                                .in(SkillConfigEntity::getDeptId, ancestorDeptIds)
                        );
                    }

                    if (grantResourceIds != null && !grantResourceIds.isEmpty()) {
                        w.or(ow -> ow.in(SkillConfigEntity::getId, grantResourceIds));
                    }
                })
                .list();
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

    /**
     * 查询所有启用的系统级技能
     */
    public List<SkillConfigEntity> listSystemEnabled() {
        return this.lambdaQuery()
                .eq(SkillConfigEntity::getScope, "SYSTEM")
                .eq(SkillConfigEntity::getEnabled, 1)
                .list();
    }

    /**
     * 通用分页查询（不分 scope）
     */
    public Page<SkillConfigEntity> listPage(int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(SkillConfigEntity::getDeleted, 0)
                .orderByDesc(SkillConfigEntity::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
    }

    /**
     * 获取技能工作区路径
     */
    public String getWorkspacePath(Long skillId) {
        SkillConfigEntity entity = this.getById(skillId);
        if (entity == null) {
            throw new RuntimeException("Skill not found: " + skillId);
        }
        String workspacePath = entity.getWorkspacePath();
        if (workspacePath == null || workspacePath.isEmpty()) {
            return "skills/" + skillId + "/";
        }
        if (!workspacePath.endsWith("/")) {
            workspacePath += "/";
        }
        return workspacePath;
    }

    /**
     * 根据 scope 类型构建完整的工作区路径
     */
    public String buildWorkspacePath(Long skillId, String scopeType, Long ownerId) {
        String basePath;
        switch (scopeType) {
            case "DEPT":
                basePath = "dept/" + ownerId + "/skills/" + skillId + "/";
                break;
            case "PROJECT":
                basePath = "project/" + ownerId + "/skills/" + skillId + "/";
                break;
            case "USER":
            default:
                basePath = "user/" + ownerId + "/skills/" + skillId + "/";
                break;
        }
        SkillConfigEntity entity = this.getById(skillId);
        if (entity != null) {
            entity.setWorkspacePath(basePath);
            this.updateById(entity);
        }
        return basePath;
    }
}
