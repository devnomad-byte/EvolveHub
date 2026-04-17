package org.evolve.domain.rbac.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.rbac.model.UserRolesEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户-角色关联数据访问层
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Repository
public class UserRolesInfra extends ServiceImpl<UserRolesInfra.UserRolesMapper, UserRolesEntity> {

    @Mapper
    public interface UserRolesMapper extends BaseMapper<UserRolesEntity> {}

    public UserRolesEntity getByUserIdAndRoleId(Long userId, Long roleId) {
        return this.lambdaQuery()
                .eq(UserRolesEntity::getUserId, userId)
                .eq(UserRolesEntity::getRoleId, roleId)
                .one();
    }

    public List<UserRolesEntity> listByUserId(Long userId) {
        return this.lambdaQuery().eq(UserRolesEntity::getUserId, userId).list();
    }

    public long countByRoleId(Long roleId) {
        return this.lambdaQuery().eq(UserRolesEntity::getRoleId, roleId).count();
    }

    public void assignRole(UserRolesEntity entity) {
        this.save(entity);
    }

    public void removeByUserIdAndRoleId(Long userId, Long roleId) {
        this.lambdaUpdate()
                .eq(UserRolesEntity::getUserId, userId)
                .eq(UserRolesEntity::getRoleId, roleId)
                .remove();
    }

    public void removeByUserId(Long userId) {
        this.lambdaUpdate().eq(UserRolesEntity::getUserId, userId).remove();
    }

    public void removeByRoleId(Long roleId) {
        this.lambdaUpdate().eq(UserRolesEntity::getRoleId, roleId).remove();
    }
}
