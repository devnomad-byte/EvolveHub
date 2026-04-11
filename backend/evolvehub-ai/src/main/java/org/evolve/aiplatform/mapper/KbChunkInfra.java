package org.evolve.aiplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.aiplatform.bean.entity.KbChunkEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 知识库切片数据访问层
 *
 * @author xiaoxin
 * @version v1.0
 * @date 2026/4/11
 */
@Repository
public class KbChunkInfra extends ServiceImpl<KbChunkInfra.KbChunkMapper, KbChunkEntity> {

    @Mapper
    interface KbChunkMapper extends BaseMapper<KbChunkEntity> {}

    // ==================== 单条查询 ====================

    public KbChunkEntity getChunkById(Long chunkId) {
        return this.lambdaQuery().eq(KbChunkEntity::getChunkId, chunkId).one();
    }

    // ==================== 列表查询 ====================

    public List<KbChunkEntity> listByDocId(Long docId) {
        return this.lambdaQuery()
                .eq(KbChunkEntity::getDocId, docId)
                .orderByAsc(KbChunkEntity::getChunkIndex)
                .list();
    }

    public Page<KbChunkEntity> pageByKbId(Long kbId, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(KbChunkEntity::getKbId, kbId)
                .page(new Page<>(pageNum, pageSize));
    }

    public long countByDocId(Long docId) {
        return this.lambdaQuery().eq(KbChunkEntity::getDocId, docId).count();
    }

    // ==================== 写入 ====================

    public void createChunk(KbChunkEntity entity) {
        this.save(entity);
    }

    public void batchCreateChunks(List<KbChunkEntity> entities) {
        this.saveBatch(entities);
    }

    public void updateChunk(KbChunkEntity entity) {
        this.lambdaUpdate()
                .eq(KbChunkEntity::getChunkId, entity.getChunkId())
                .update(entity);
    }

    public void deleteByDocId(Long docId) {
        this.lambdaUpdate().eq(KbChunkEntity::getDocId, docId).remove();
    }

    public void deleteByKbId(Long kbId) {
        this.lambdaUpdate().eq(KbChunkEntity::getKbId, kbId).remove();
    }
}