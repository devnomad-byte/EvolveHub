package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.ModelProviderEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 模型提供商数据访问层
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/13
 */
@Repository
public class ModelProviderInfra extends ServiceImpl<ModelProviderInfra.ModelProviderMapper, ModelProviderEntity> {

    @Mapper
    public interface ModelProviderMapper extends BaseMapper<ModelProviderEntity> {}

    public ModelProviderEntity getById(Long id) {
        return this.getById(id);
    }

    public ModelProviderEntity getByName(String name) {
        return this.lambdaQuery().eq(ModelProviderEntity::getName, name).one();
    }

    public void create(ModelProviderEntity entity) {
        this.save(entity);
    }

    public void update(ModelProviderEntity entity) {
        this.updateById(entity);
    }

    public void delete(Long id) {
        this.removeById(id);
    }

    /**
     * 获取全部启用的提供商（按 sort 排序）
     */
    public List<ModelProviderEntity> listAllEnabled() {
        return this.lambdaQuery()
                .eq(ModelProviderEntity::getEnabled, 1)
                .orderByAsc(ModelProviderEntity::getSort)
                .list();
    }
}
