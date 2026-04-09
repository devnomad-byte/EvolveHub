package org.evolve.auth.user.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.auth.user.model.DeptEntity;
import org.springframework.stereotype.Repository;

/**
 * @className DeptInfra
 * @description 部门持久化 
 * @author zhao
 * @date 2026/4/9 15:36
 * @version v1.0
**/
@Repository
public class DeptInfra extends ServiceImpl<DeptInfra.DeptMapper, DeptEntity> {

    @Mapper
    interface DeptMapper extends BaseMapper<DeptEntity> {}

    // 根据部门名称获取部门
    public DeptEntity getDeptByName(String name){
        return this.lambdaQuery().eq(DeptEntity::getDeptName, name).one();
    }

    // 创建部门
    public void createDept(DeptEntity deptEntity){
        this.save(deptEntity);
    }

    // 根据父部门ID获取部门
    public DeptEntity getDeptByParentId(Long parentId){
        return this.lambdaQuery().eq(DeptEntity::getParentId, parentId).one();
    }
}
