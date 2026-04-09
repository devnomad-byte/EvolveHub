package org.evolve.auth.user.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.auth.user.model.RolePermissionsEntity;
import org.springframework.stereotype.Repository;

/**
 * 角色-权限关联数据访问层
 * <p>
 * 封装 t_role_permission 表的所有数据库操作。
 * 一个角色可拥有多个功能权限，一个权限可被多个角色持有（多对多）。
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Repository
public class RolePermissionsInfra extends ServiceImpl<RolePermissionsInfra.RolePermissionsMapper, RolePermissionsEntity> {

    @Mapper
    interface RolePermissionsMapper extends BaseMapper<RolePermissionsEntity> {}

    // ==================== 查询 ====================

    /**
     * 根据 roleId + permissionId 联合查询（防止重复分配）
     *
     * @param roleId       角色 ID
     * @param permissionId 权限 ID
     * @return 关联记录，不存在返回 null
     */
    public RolePermissionsEntity getByRoleIdAndPermissionId(Long roleId, Long permissionId) {
        return this.lambdaQuery()
                .eq(RolePermissionsEntity::getRoleId, roleId)
                .eq(RolePermissionsEntity::getPermissionId, permissionId)
                .one();
    }

    // ==================== 统计 ====================

    /**
     * 统计指定权限被多少角色持有（删除权限前的关联检查）
     *
     * @param permissionId 权限 ID
     * @return 持有该权限的角色数
     */
    public long countByPermissionId(Long permissionId) {
        return this.lambdaQuery().eq(RolePermissionsEntity::getPermissionId, permissionId).count();
    }

    // ==================== 写入 ====================

    /**
     * 为角色分配权限
     *
     * @param entity 关联实体（roleId + permissionId）
     */
    public void assignPermission(RolePermissionsEntity entity) {
        this.save(entity);
    }

    /**
     * 移除角色的指定权限
     *
     * @param roleId       角色 ID
     * @param permissionId 权限 ID
     */
    public void removeByRoleIdAndPermissionId(Long roleId, Long permissionId) {
        this.lambdaUpdate()
                .eq(RolePermissionsEntity::getRoleId, roleId)
                .eq(RolePermissionsEntity::getPermissionId, permissionId)
                .remove();
    }

    /**
     * 移除指定角色的全部权限关联（删除角色时级联清理）
     *
     * @param roleId 角色 ID
     */
    public void removeByRoleId(Long roleId) {
        this.lambdaUpdate().eq(RolePermissionsEntity::getRoleId, roleId).remove();
    }

    /**
     * 移除指定权限的全部角色关联（删除权限时级联清理）
     *
     * @param permissionId 权限 ID
     */
    public void removeByPermissionId(Long permissionId) {
        this.lambdaUpdate().eq(RolePermissionsEntity::getPermissionId, permissionId).remove();
    }
}
