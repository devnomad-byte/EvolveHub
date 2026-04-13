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

    /**
     * 按 scope 分页查询
     *
     * @param scope    资源范围（SYSTEM / USER）
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public Page<ModelConfigEntity> listPageByScope(String scope, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(ModelConfigEntity::getScope, scope)
                .page(new Page<>(pageNum, pageSize));
    }

    /**
     * 按 ownerId 分页查询（用户级资源）
     *
     * @param ownerId  资源所有者 ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public Page<ModelConfigEntity> listPageByOwnerId(Long ownerId, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(ModelConfigEntity::getScope, "USER")
                .eq(ModelConfigEntity::getOwnerId, ownerId)
                .page(new Page<>(pageNum, pageSize));
    }

    /**
     * 查询用户可用的系统级模型（通过授权 ID 列表）
     *
     * @param resourceIds 已授权的资源 ID 列表
     * @return 模型配置列表
     */
    /**
     * 获取全局唯一的系统级向量模型
     *
     * @return 向量模型配置，不存在时返回 null
     */
    public ModelConfigEntity getSystemEmbeddingModel() {
        return this.lambdaQuery()
                .eq(ModelConfigEntity::getScope, "SYSTEM")
                .eq(ModelConfigEntity::getModelType, "embedding")
                .last("LIMIT 1")
                .one();
    }

    public List<ModelConfigEntity> listByIdsAndScope(List<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return List.of();
        }
        return this.lambdaQuery()
                .eq(ModelConfigEntity::getScope, "SYSTEM")
                .eq(ModelConfigEntity::getEnabled, 1)
                .in(ModelConfigEntity::getId, resourceIds)
                .list();
    }
}
