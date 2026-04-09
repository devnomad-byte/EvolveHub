package org.evolve.auth.user.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.auth.user.model.RolesEntity;
import org.springframework.stereotype.Repository;

/**
 * 角色数据访问层
 * <p>
 * 封装 t_role 表的所有数据库操作。角色是 RBAC 的核心枢纽，
 * 向上关联用户（t_user_role），向下关联功能权限（t_role_permission）和数据权限（t_role_data_scope）。
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Repository
public class RolesInfra extends ServiceImpl<RolesInfra.RolesMapper, RolesEntity> {

    @Mapper
    interface RolesMapper extends BaseMapper<RolesEntity> {}

    // ==================== 单条查询 ====================

    /**
     * 根据主键查询角色
     *
     * @param id 角色 ID
     * @return 角色实体，不存在返回 null
     */
    public RolesEntity getRoleById(Long id) {
        return this.getById(id);
    }

    /**
     * 根据角色名称查询（唯一性校验）
     *
     * @param roleName 角色名称
     * @return 角色实体，不存在返回 null
     */
    public RolesEntity getByRoleName(String roleName) {
        return this.lambdaQuery().eq(RolesEntity::getRoleName, roleName).one();
    }

    /**
     * 根据角色编码查询（唯一性校验，如 ROLE_ADMIN）
     *
     * @param roleCode 角色编码
     * @return 角色实体，不存在返回 null
     */
    public RolesEntity getByRoleCode(String roleCode) {
        return this.lambdaQuery().eq(RolesEntity::getRoleCode, roleCode).one();
    }

    // ==================== 写入 ====================

    /**
     * 新增角色
     *
     * @param entity 角色实体（需预先设好所有字段）
     */
    public void createRole(RolesEntity entity) {
        this.save(entity);
    }

    /**
     * 按主键更新角色（仅更新非 null 字段）
     *
     * @param entity 角色实体（id 必填，其余字段按需设值）
     */
    public void updateRole(RolesEntity entity) {
        this.updateById(entity);
    }

    /**
     * 逻辑删除角色
     *
     * @param id 角色 ID
     */
    public void deleteRole(Long id) {
        this.removeById(id);
    }

    // ==================== 分页 ====================

    /**
     * 分页查询角色列表
     *
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public Page<RolesEntity> listPage(int pageNum, int pageSize) {
        return this.page(new Page<>(pageNum, pageSize));
    }
}
