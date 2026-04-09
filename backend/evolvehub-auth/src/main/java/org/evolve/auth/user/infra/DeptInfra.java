package org.evolve.auth.user.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.auth.user.model.DeptEntity;

/**
 * @className DeptInfra
 * @description 部门持久化 
 * @author zhao
 * @date 2026/4/9 15:36
 * @version v1.0
**/
@Mapper
public interface DeptInfra extends BaseMapper<DeptEntity> {

}
