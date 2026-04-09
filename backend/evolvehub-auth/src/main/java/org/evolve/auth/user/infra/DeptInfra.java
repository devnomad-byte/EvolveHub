package org.evolve.auth.user.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.auth.user.model.DeptEntity;
import org.springframework.stereotype.Repository;

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
    interface DeptMapper extends BaseMapper<DeptEntity> {}

    // ==================== 单条查询 ====================

    /**
     * 根据主键查询部门
     *
     * @param id 部门 ID
     * @return 部门实体，不存在返回 null
     */
    public DeptEntity getDeptById(Long id) {
        return this.getById(id);
    }

    /**
     * 根据部门名称查询（全局唯一性校验）
     *
     * @param name 部门名称
     * @return 部门实体，不存在返回 null
     */
    public DeptEntity getDeptByName(String name) {
        return this.lambdaQuery().eq(DeptEntity::getDeptName, name).one();
    }

    /**
     * 根据父部门 ID 和部门名称查询（同级唯一性校验）
     *
     * @param parentId 父部门 ID
     * @param deptName 部门名称
     * @return 部门实体，不存在返回 null
     */
    public DeptEntity getByParentIdAndName(Long parentId, String deptName) {
        return this.lambdaQuery()
                .eq(DeptEntity::getParentId, parentId)
                .eq(DeptEntity::getDeptName, deptName)
                .one();
    }

    // ==================== 存在性 / 统计 ====================

    /**
     * 判断指定部门下是否存在子部门（删除部门前的关联检查）
     *
     * @param parentId 父部门 ID
     * @return true-存在子部门，false-无子部门
     */
    public boolean existsChildDept(Long parentId) {
        return this.lambdaQuery().eq(DeptEntity::getParentId, parentId).exists();
    }

    // ==================== 写入 ====================

    /**
     * 新增部门
     *
     * @param entity 部门实体（需预先设好所有字段）
     */
    public void createDept(DeptEntity entity) {
        this.save(entity);
    }

    /**
     * 按主键更新部门（仅更新非 null 字段）
     *
     * @param entity 部门实体（id 必填，其余字段按需设值）
     */
    public void updateDept(DeptEntity entity) {
        this.updateById(entity);
    }

    /**
     * 逻辑删除部门
     *
     * @param id 部门 ID
     */
    public void deleteDept(Long id) {
        this.removeById(id);
    }

    // ==================== 分页 ====================

    /**
     * 分页查询部门列表
     *
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public Page<DeptEntity> listPage(int pageNum, int pageSize) {
        return this.page(new Page<>(pageNum, pageSize));
    }
}
