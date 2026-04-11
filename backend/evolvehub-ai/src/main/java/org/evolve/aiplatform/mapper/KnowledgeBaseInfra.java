package org.evolve.aiplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.aiplatform.bean.entity.KnowledgeBaseEntity;
import org.springframework.stereotype.Repository;

/**
 * 知识库数据访问层
 *
 * @author xiaoxin
 * @version v1.0
 * @date 2026/4/11
 */
@Repository
public class KnowledgeBaseInfra extends ServiceImpl<KnowledgeBaseInfra.KnowledgeBaseMapper, KnowledgeBaseEntity> {

    @Mapper
    interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBaseEntity> {}

    // ==================== 单条查询 ====================

    public KnowledgeBaseEntity getById(Long id) {
        return this.lambdaQuery().eq(KnowledgeBaseEntity::getId, id).one();
    }

    // ==================== 列表查询 ====================

    public Page<KnowledgeBaseEntity> listByOwnerId(Long ownerId, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(KnowledgeBaseEntity::getOwnerId, ownerId)
                .page(new Page<>(pageNum, pageSize));
    }

    public Page<KnowledgeBaseEntity> listByDeptId(Long deptId, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(KnowledgeBaseEntity::getDeptId, deptId)
                .page(new Page<>(pageNum, pageSize));
    }

    public Page<KnowledgeBaseEntity> listPage(int pageNum, int pageSize) {
        return this.page(new Page<>(pageNum, pageSize));
    }

    // ==================== 写入 ====================

    public void createKb(KnowledgeBaseEntity entity) {
        this.save(entity);
    }

    public void updateKb(KnowledgeBaseEntity entity) {
        this.updateById(entity);
    }

    public void deleteKb(Long id) {
        this.removeById(id);
    }
}