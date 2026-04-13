package org.evolve.domain.rbac.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.rbac.model.RolePermissionsEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色-权限关联数据访问层
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Repository
public class RolePermissionsInfra extends ServiceImpl<RolePermissionsInfra.RolePermissionsMapper, RolePermissionsEntity> {

    @Mapper
    interface RolePermissionsMapper extends BaseMapper<RolePermissionsEntity> {}

    public RolePermissionsEntity getByRoleIdAndPermissionId(Long roleId, Long permissionId) {
        return this.lambdaQuery()
                .eq(RolePermissionsEntity::getRoleId, roleId)
                .eq(RolePermissionsEntity::getPermissionId, permissionId)
                .one();
    }

    public List<RolePermissionsEntity> listByRoleId(Long roleId) {
        return this.lambdaQuery().eq(RolePermissionsEntity::getRoleId, roleId).list();
    }

    public long countByPermissionId(Long permissionId) {
        return this.lambdaQuery().eq(RolePermissionsEntity::getPermissionId, permissionId).count();
    }

    public void assignPermission(RolePermissionsEntity entity) {
        this.save(entity);
    }

    public void removeByRoleIdAndPermissionId(Long roleId, Long permissionId) {
        this.lambdaUpdate()
                .eq(RolePermissionsEntity::getRoleId, roleId)
                .eq(RolePermissionsEntity::getPermissionId, permissionId)
                .remove();
    }

    public void removeByRoleId(Long roleId) {
        this.lambdaUpdate().eq(RolePermissionsEntity::getRoleId, roleId).remove();
    }

    public void removeByPermissionId(Long permissionId) {
        this.lambdaUpdate().eq(RolePermissionsEntity::getPermissionId, permissionId).remove();
    }
}
