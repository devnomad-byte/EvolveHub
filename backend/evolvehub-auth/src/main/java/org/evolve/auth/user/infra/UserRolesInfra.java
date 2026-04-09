package org.evolve.auth.user.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.auth.user.model.UserRolesEntity;
import org.springframework.stereotype.Repository;

/**
 * 用户-角色关联数据访问层
 * <p>
 * 封装 t_user_role 表的所有数据库操作。
 * 一个用户可拥有多个角色，一个角色可被多个用户持有（多对多）。
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Repository
public class UserRolesInfra extends ServiceImpl<UserRolesInfra.UserRolesMapper, UserRolesEntity> {

    @Mapper
    interface UserRolesMapper extends BaseMapper<UserRolesEntity> {}

    // ==================== 查询 ====================

    /**
     * 根据 userId + roleId 联合查询（防止重复分配）
     *
     * @param userId 用户 ID
     * @param roleId 角色 ID
     * @return 关联记录，不存在返回 null
     */
    public UserRolesEntity getByUserIdAndRoleId(Long userId, Long roleId) {
        return this.lambdaQuery()
                .eq(UserRolesEntity::getUserId, userId)
                .eq(UserRolesEntity::getRoleId, roleId)
                .one();
    }

    // ==================== 统计 ====================

    /**
     * 统计指定角色被多少用户持有（删除角色前的关联检查）
     *
     * @param roleId 角色 ID
     * @return 持有该角色的用户数
     */
    public long countByRoleId(Long roleId) {
        return this.lambdaQuery().eq(UserRolesEntity::getRoleId, roleId).count();
    }

    // ==================== 写入 ====================

    /**
     * 为用户分配角色
     *
     * @param entity 关联实体（userId + roleId）
     */
    public void assignRole(UserRolesEntity entity) {
        this.save(entity);
    }

    /**
     * 移除用户的指定角色
     *
     * @param userId 用户 ID
     * @param roleId 角色 ID
     */
    public void removeByUserIdAndRoleId(Long userId, Long roleId) {
        this.lambdaUpdate()
                .eq(UserRolesEntity::getUserId, userId)
                .eq(UserRolesEntity::getRoleId, roleId)
                .remove();
    }

    /**
     * 移除指定用户的全部角色（删除用户时级联清理）
     *
     * @param userId 用户 ID
     */
    public void removeByUserId(Long userId) {
        this.lambdaUpdate().eq(UserRolesEntity::getUserId, userId).remove();
    }

    /**
     * 移除指定角色的全部用户关联（删除角色时级联清理）
     *
     * @param roleId 角色 ID
     */
    public void removeByRoleId(Long roleId) {
        this.lambdaUpdate().eq(UserRolesEntity::getRoleId, roleId).remove();
    }
}
