package org.evolve.auth.user.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.auth.user.model.PermissionsEntity;
import org.springframework.stereotype.Repository;

/**
 * 权限数据访问层
 * <p>
 * 封装 t_permission 表的所有数据库操作。权限为树形结构（MENU → BUTTON / API），
 * 通过 parentId 构建层级，permCode 全局唯一，用于后端接口鉴权。
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Repository
public class PermissionsInfra extends ServiceImpl<PermissionsInfra.PermissionsMapper, PermissionsEntity> {

    @Mapper
    interface PermissionsMapper extends BaseMapper<PermissionsEntity> {}

    // ==================== 单条查询 ====================

    /**
     * 根据主键查询权限
     *
     * @param id 权限 ID
     * @return 权限实体，不存在返回 null
     */
    public PermissionsEntity getPermissionById(Long id) {
        return this.getById(id);
    }

    /**
     * 根据权限编码查询（唯一性校验，如 user:list、kb:read）
     *
     * @param permCode 权限编码
     * @return 权限实体，不存在返回 null
     */
    public PermissionsEntity getByPermCode(String permCode) {
        return this.lambdaQuery().eq(PermissionsEntity::getPermCode, permCode).one();
    }

    // ==================== 存在性 / 统计 ====================

    /**
     * 判断指定权限下是否存在子权限（删除权限前的关联检查）
     *
     * @param parentId 父权限 ID
     * @return true-存在子权限，false-无子权限
     */
    public boolean existsChildPermission(Long parentId) {
        return this.lambdaQuery().eq(PermissionsEntity::getParentId, parentId).exists();
    }

    // ==================== 写入 ====================

    /**
     * 新增权限
     *
     * @param entity 权限实体（需预先设好所有字段）
     */
    public void createPermission(PermissionsEntity entity) {
        this.save(entity);
    }

    /**
     * 按主键更新权限（仅更新非 null 字段）
     *
     * @param entity 权限实体（id 必填，其余字段按需设值）
     */
    public void updatePermission(PermissionsEntity entity) {
        this.updateById(entity);
    }

    /**
     * 逻辑删除权限
     *
     * @param id 权限 ID
     */
    public void deletePermission(Long id) {
        this.removeById(id);
    }

    // ==================== 分页 ====================

    /**
     * 分页查询权限列表
     *
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public Page<PermissionsEntity> listPage(int pageNum, int pageSize) {
        return this.page(new Page<>(pageNum, pageSize));
    }
}
