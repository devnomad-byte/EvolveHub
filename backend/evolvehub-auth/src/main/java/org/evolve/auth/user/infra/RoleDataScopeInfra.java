package org.evolve.auth.user.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.auth.user.model.RoleDataScopeEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色自定义数据范围数据访问层
 * <p>
 * 封装 t_role_data_scope 表的所有数据库操作。
 * 仅当 RolesEntity.dataScope = 5（自定义）时生效，指定该角色可见的部门列表。
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Repository
public class RoleDataScopeInfra extends ServiceImpl<RoleDataScopeInfra.RoleDataScopeMapper, RoleDataScopeEntity> {

    @Mapper
    interface RoleDataScopeMapper extends BaseMapper<RoleDataScopeEntity> {}

    // ==================== 查询 ====================

    /**
     * 根据 roleId + deptId 联合查询
     *
     * @param roleId 角色 ID
     * @param deptId 部门 ID
     * @return 关联记录，不存在返回 null
     */
    public RoleDataScopeEntity getByRoleIdAndDeptId(Long roleId, Long deptId) {
        return this.lambdaQuery()
                .eq(RoleDataScopeEntity::getRoleId, roleId)
                .eq(RoleDataScopeEntity::getDeptId, deptId)
                .one();
    }

    /**
     * 查询指定角色的全部自定义数据范围
     *
     * @param roleId 角色 ID
     * @return 该角色关联的全部部门列表
     */
    public List<RoleDataScopeEntity> listByRoleId(Long roleId) {
        return this.lambdaQuery().eq(RoleDataScopeEntity::getRoleId, roleId).list();
    }

    // ==================== 写入 ====================

    /**
     * 新增一条自定义数据范围记录
     *
     * @param entity 关联实体（roleId + deptId）
     */
    public void assignDataScope(RoleDataScopeEntity entity) {
        this.save(entity);
    }

    /**
     * 移除指定角色的全部自定义数据范围（重新设置前先清除旧数据 / 删除角色时级联清理）
     *
     * @param roleId 角色 ID
     */
    public void removeByRoleId(Long roleId) {
        this.lambdaUpdate().eq(RoleDataScopeEntity::getRoleId, roleId).remove();
    }
}
