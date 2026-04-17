package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.ResourceGrantEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 资源授权数据访问层
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/13
 */
@Repository
public class ResourceGrantInfra extends ServiceImpl<ResourceGrantInfra.ResourceGrantMapper, ResourceGrantEntity> {

    @Mapper
    public interface ResourceGrantMapper extends BaseMapper<ResourceGrantEntity> {}

    /**
     * 检查用户是否已被授权某个资源
     *
     * @param userId       用户 ID
     * @param resourceType 资源类型（MODEL / SKILL / MCP）
     * @param resourceId   资源 ID
     * @return true 表示已授权
     */
    public boolean exists(Long userId, String resourceType, Long resourceId) {
        return this.lambdaQuery()
                .eq(ResourceGrantEntity::getUserId, userId)
                .eq(ResourceGrantEntity::getResourceType, resourceType)
                .eq(ResourceGrantEntity::getResourceId, resourceId)
                .exists();
    }

    /**
     * 创建授权记录
     *
     * @param entity 授权实体
     */
    public void createGrant(ResourceGrantEntity entity) {
        this.save(entity);
    }

    /**
     * 撤销授权
     *
     * @param userId       用户 ID
     * @param resourceType 资源类型
     * @param resourceId   资源 ID
     */
    public void revokeGrant(Long userId, String resourceType, Long resourceId) {
        this.lambdaUpdate()
                .eq(ResourceGrantEntity::getUserId, userId)
                .eq(ResourceGrantEntity::getResourceType, resourceType)
                .eq(ResourceGrantEntity::getResourceId, resourceId)
                .remove();
    }

    /**
     * 查询用户被授权的某类资源 ID 列表
     *
     * @param userId       用户 ID
     * @param resourceType 资源类型
     * @return 资源 ID 列表
     */
    public List<Long> listGrantedResourceIds(Long userId, String resourceType) {
        return this.lambdaQuery()
                .eq(ResourceGrantEntity::getUserId, userId)
                .eq(ResourceGrantEntity::getResourceType, resourceType)
                .list()
                .stream()
                .map(ResourceGrantEntity::getResourceId)
                .toList();
    }

    /**
     * 分页查询授权列表（管理员用）
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public Page<ResourceGrantEntity> listPage(int pageNum, int pageSize) {
        return this.page(new Page<>(pageNum, pageSize));
    }

    /**
     * 按用户 ID 分页查询授权列表
     *
     * @param userId   用户 ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public Page<ResourceGrantEntity> listPageByUserId(Long userId, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(ResourceGrantEntity::getUserId, userId)
                .page(new Page<>(pageNum, pageSize));
    }

    /**
     * 按资源查询所有授权记录
     *
     * @param resourceType 资源类型（MCP / SKILL / MODEL）
     * @param resourceId   资源 ID
     * @return 授权记录列表
     */
    public List<ResourceGrantEntity> listByResource(String resourceType, Long resourceId) {
        return this.lambdaQuery()
                .eq(ResourceGrantEntity::getResourceType, resourceType)
                .eq(ResourceGrantEntity::getResourceId, resourceId)
                .list();
    }

    /**
     * 查询用户被授权的某类资源 ID 列表（包含用户直接授权 + 用户所属部门的授权）
     *
     * @param userId       用户 ID
     * @param deptId       用户部门 ID（可为 null）
     * @param resourceType 资源类型
     * @return 资源 ID 列表（去重）
     */
    public List<Long> listVisibleGrantedResourceIds(Long userId, Long deptId, String resourceType) {
        return this.lambdaQuery()
                .eq(ResourceGrantEntity::getResourceType, resourceType)
                .and(wrapper -> {
                    wrapper.eq(ResourceGrantEntity::getUserId, userId);
                    if (deptId != null) {
                        wrapper.or().eq(ResourceGrantEntity::getDeptId, deptId);
                    }
                })
                .list()
                .stream()
                .map(ResourceGrantEntity::getResourceId)
                .distinct()
                .toList();
    }

    /**
     * 按部门查询授权的资源 ID 列表
     *
     * @param deptId       部门 ID
     * @param resourceType 资源类型
     * @return 资源 ID 列表
     */
    public List<Long> listByDept(Long deptId, String resourceType) {
        return this.lambdaQuery()
                .eq(ResourceGrantEntity::getDeptId, deptId)
                .eq(ResourceGrantEntity::getResourceType, resourceType)
                .list()
                .stream()
                .map(ResourceGrantEntity::getResourceId)
                .toList();
    }

    /**
     * 按角色查询授权的资源 ID 列表
     *
     * @param roleId       角色 ID
     * @param resourceType 资源类型
     * @return 资源 ID 列表
     */
    public List<Long> listByRole(Long roleId, String resourceType) {
        return this.lambdaQuery()
                .eq(ResourceGrantEntity::getRoleId, roleId)
                .eq(ResourceGrantEntity::getResourceType, resourceType)
                .list()
                .stream()
                .map(ResourceGrantEntity::getResourceId)
                .toList();
    }
}
