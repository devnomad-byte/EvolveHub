package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.EnvVarEntity;
import org.springframework.stereotype.Repository;

@Repository
public class EnvVarInfra extends ServiceImpl<EnvVarInfra.EnvVarMapper, EnvVarEntity> {

    @Mapper
    public interface EnvVarMapper extends BaseMapper<EnvVarEntity> {}

    public EnvVarEntity getById(Long id) {
        return this.lambdaQuery().eq(EnvVarEntity::getId, id).one();
    }

    public EnvVarEntity getByKey(String varKey) {
        return this.lambdaQuery().eq(EnvVarEntity::getVarKey, varKey).one();
    }

    public Page<EnvVarEntity> listPage(String varGroup, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(EnvVarEntity::getDeleted, 0)
                .eq(varGroup != null && !varGroup.isEmpty(), EnvVarEntity::getVarGroup, varGroup)
                .orderByAsc(EnvVarEntity::getSort)
                .orderByDesc(EnvVarEntity::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
    }
}
