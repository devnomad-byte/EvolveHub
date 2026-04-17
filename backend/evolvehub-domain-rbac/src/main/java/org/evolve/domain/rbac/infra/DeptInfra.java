package org.evolve.domain.rbac.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.rbac.model.DeptEntity;
import org.evolve.common.datascope.DataScopeContextHolder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门数据访问层
 * <p>
 * 封装 t_dept 表的所有数据库操作。部门为树形结构，通过 parentId 构建层级关系，
 * 同时作为数据权限的核心维度（用户归属部门 → 角色关联数据范围）。
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/9
 */
@Repository
public class DeptInfra extends ServiceImpl<DeptInfra.DeptMapper, DeptEntity> {

    @Mapper
    public interface DeptMapper extends BaseMapper<DeptEntity> {}

    // ==================== 单条查询 ====================

    public DeptEntity getDeptById(Long id) {
        return this.getById(id);
    }

    public DeptEntity getDeptByName(String name) {
        return this.lambdaQuery().eq(DeptEntity::getDeptName, name).one();
    }

    public DeptEntity getByParentIdAndName(Long parentId, String deptName) {
        return this.lambdaQuery()
                .eq(DeptEntity::getParentId, parentId)
                .eq(DeptEntity::getDeptName, deptName)
                .one();
    }

    // ==================== 存在性 / 统计 ====================

    public boolean existsChildDept(Long parentId) {
        return this.lambdaQuery().eq(DeptEntity::getParentId, parentId).exists();
    }

    // ==================== 写入 ====================

    public void createDept(DeptEntity entity) {
        this.save(entity);
    }

    public void updateDept(DeptEntity entity) {
        this.updateById(entity);
    }

    public void deleteDept(Long id) {
        this.removeById(id);
    }

    /**
     * 获取部门及其所有祖先部门的 ID 列表（含自身）
     * <p>
     * 从当前部门出发，沿 parentId 向上遍历直到顶级（parentId=0），
     * 返回所有途经部门的 ID。
     * </p>
     *
     * @param deptId 起始部门 ID
     * @return 包含自身及所有祖先部门 ID 的列表，deptId 无效时返回空列表
     */
    public List<Long> getAncestorDeptIds(Long deptId) {
        List<Long> ids = new ArrayList<>();
        if (deptId == null) {
            return ids;
        }
        Long currentId = deptId;
        while (currentId != null && currentId > 0) {
            ids.add(currentId);
            DeptEntity dept = this.getById(currentId);
            if (dept == null || dept.getParentId() == null || dept.getParentId() == 0L) {
                break;
            }
            currentId = dept.getParentId();
        }
        return ids;
    }

    // ==================== 分页 ====================

    public Page<DeptEntity> listPage(int pageNum, int pageSize) {
        DataScopeContextHolder.enableFilter();
        try {
            return this.page(new Page<>(pageNum, pageSize));
        } finally {
            DataScopeContextHolder.disableFilter();
        }
    }
}
