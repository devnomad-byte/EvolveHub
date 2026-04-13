package org.evolve.domain.rbac.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.rbac.model.RolesEntity;
import org.springframework.stereotype.Repository;

/**
 * 角色数据访问层
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Repository
public class RolesInfra extends ServiceImpl<RolesInfra.RolesMapper, RolesEntity> {

    @Mapper
    interface RolesMapper extends BaseMapper<RolesEntity> {}

    public RolesEntity getRoleById(Long id) {
        return this.getById(id);
    }

    public RolesEntity getByRoleName(String roleName) {
        return this.lambdaQuery().eq(RolesEntity::getRoleName, roleName).one();
    }

    public RolesEntity getByRoleCode(String roleCode) {
        return this.lambdaQuery().eq(RolesEntity::getRoleCode, roleCode).one();
    }

    public void createRole(RolesEntity entity) {
        this.save(entity);
    }

    public void updateRole(RolesEntity entity) {
        this.updateById(entity);
    }

    public void deleteRole(Long id) {
        this.removeById(id);
    }

    public Page<RolesEntity> listPage(int pageNum, int pageSize) {
        return this.page(new Page<>(pageNum, pageSize));
    }
}
