package org.evolve.aiplatform.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.aiplatform.model.ModelConfigEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 模型配置数据访问层
 * <p>
 * 封装 eh_model_config 表的所有数据库操作，对外提供业务语义化的查询和写入方法，
 * 上层 Manager 只通过本类访问模型配置数据，不直接接触 SQL / MyBatis API。
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/10
 */
@Repository
public class ModelConfigInfra extends ServiceImpl<ModelConfigInfra.ModelConfigMapper, ModelConfigEntity> {

    @Mapper
    interface ModelConfigMapper extends BaseMapper<ModelConfigEntity> {}

    // ==================== 单条查询 ====================

    /**
     * 根据主键查询模型配置
     *
     * @param id 模型配置 ID
     * @return 模型配置实体，不存在返回 null
     */
    public ModelConfigEntity getModelConfigById(Long id) {
        return this.getById(id);
    }

    /**
     * 根据模型名称查询配置
     *
     * @param name 模型名称
     * @return 模型配置实体，不存在返回 null
     */
    public ModelConfigEntity getByName(String name) {
        return this.lambdaQuery().eq(ModelConfigEntity::getName, name).one();
    }

    /**
     * 根据提供商查询配置列表
     *
     * @param provider 提供商名称
     * @return 该提供商下的所有模型配置
     */
    public List<ModelConfigEntity> listByProvider(String provider) {
        return this.lambdaQuery().eq(ModelConfigEntity::getProvider, provider).list();
    }

    // ==================== 写入 ====================

    /**
     * 新增模型配置
     *
     * @param entity 模型配置实体（需预先设好所有字段）
     */
    public void createModelConfig(ModelConfigEntity entity) {
        this.save(entity);
    }

    /**
     * 按主键更新模型配置（仅更新非 null 字段）
     *
     * @param entity 模型配置实体（id 必填，其余字段按需设值）
     */
    public void updateModelConfig(ModelConfigEntity entity) {
        this.updateById(entity);
    }

    /**
     * 逻辑删除模型配置
     *
     * @param id 模型配置 ID
     */
    public void deleteModelConfig(Long id) {
        this.removeById(id);
    }

    // ==================== 分页 ====================

    /**
     * 分页查询模型配置列表
     *
     * @param pageNum  页码（从 1 开始）
     * @param pageSize 每页条数
     * @return 分页结果
     */
    public Page<ModelConfigEntity> listPage(int pageNum, int pageSize) {
        return this.page(new Page<>(pageNum, pageSize));
    }
}
