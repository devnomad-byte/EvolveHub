package org.evolve.common.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.common.model.RoleDataScopeEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色自定义数据范围数据访问层
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Repository
public class RoleDataScopeInfra extends ServiceImpl<RoleDataScopeInfra.RoleDataScopeMapper, RoleDataScopeEntity> {

    @Mapper
    interface RoleDataScopeMapper extends BaseMapper<RoleDataScopeEntity> {}

    public RoleDataScopeEntity getByRoleIdAndDeptId(Long roleId, Long deptId) {
        return this.lambdaQuery()
                .eq(RoleDataScopeEntity::getRoleId, roleId)
                .eq(RoleDataScopeEntity::getDeptId, deptId)
                .one();
    }

    public List<RoleDataScopeEntity> listByRoleId(Long roleId) {
        return this.lambdaQuery().eq(RoleDataScopeEntity::getRoleId, roleId).list();
    }

    public void assignDataScope(RoleDataScopeEntity entity) {
        this.save(entity);
    }

    public void removeByRoleId(Long roleId) {
        this.lambdaUpdate().eq(RoleDataScopeEntity::getRoleId, roleId).remove();
    }
}
