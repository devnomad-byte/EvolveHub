package org.evolve.common.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.common.model.ModelConfigEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 模型配置数据访问层
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/10
 */
@Repository
public class ModelConfigInfra extends ServiceImpl<ModelConfigInfra.ModelConfigMapper, ModelConfigEntity> {

    @Mapper
    interface ModelConfigMapper extends BaseMapper<ModelConfigEntity> {}

    public ModelConfigEntity getModelConfigById(Long id) {
        return this.getById(id);
    }

    public ModelConfigEntity getByName(String name) {
        return this.lambdaQuery().eq(ModelConfigEntity::getName, name).one();
    }

    public List<ModelConfigEntity> listByProvider(String provider) {
        return this.lambdaQuery().eq(ModelConfigEntity::getProvider, provider).list();
    }

    public void createModelConfig(ModelConfigEntity entity) {
        this.save(entity);
    }

    public void updateModelConfig(ModelConfigEntity entity) {
        this.updateById(entity);
    }

    public void deleteModelConfig(Long id) {
        this.removeById(id);
    }

    public Page<ModelConfigEntity> listPage(int pageNum, int pageSize) {
        return this.page(new Page<>(pageNum, pageSize));
    }
}
