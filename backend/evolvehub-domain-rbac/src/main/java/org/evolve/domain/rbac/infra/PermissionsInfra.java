package org.evolve.domain.rbac.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.rbac.model.PermissionsEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 权限数据访问层
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Repository
public class PermissionsInfra extends ServiceImpl<PermissionsInfra.PermissionsMapper, PermissionsEntity> {

    @Mapper
    public interface PermissionsMapper extends BaseMapper<PermissionsEntity> {}

    public PermissionsEntity getPermissionById(Long id) {
        return this.getById(id);
    }

    public PermissionsEntity getByPermCode(String permCode) {
        return this.lambdaQuery().eq(PermissionsEntity::getPermCode, permCode).one();
    }

    public boolean existsChildPermission(Long parentId) {
        return this.lambdaQuery().eq(PermissionsEntity::getParentId, parentId).exists();
    }

    public void createPermission(PermissionsEntity entity) {
        this.save(entity);
    }

    public void updatePermission(PermissionsEntity entity) {
        this.updateById(entity);
    }

    public void deletePermission(Long id) {
        this.removeById(id);
    }

    public Page<PermissionsEntity> listPage(int pageNum, int pageSize) {
        return this.page(new Page<>(pageNum, pageSize));
    }

    /**
     * 查询所有菜单（MENU 类型）
     *
     * @return 菜单列表
     */
    public List<PermissionsEntity> listMenus() {
        return this.lambdaQuery()
                .eq(PermissionsEntity::getPermType, "MENU")
                .orderByAsc(PermissionsEntity::getSort)
                .list();
    }

    /**
     * 查询所有桌面图标
     *
     * @return 桌面图标列表
     */
    public List<PermissionsEntity> listDesktopIcons() {
        return this.lambdaQuery()
                .eq(PermissionsEntity::getIsDesktopIcon, 1)
                .orderByAsc(PermissionsEntity::getSort)
                .list();
    }
}
