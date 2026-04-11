package org.evolve.aiplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.aiplatform.bean.entity.KbDocumentEntity;
import org.springframework.stereotype.Repository;

/**
 * 知识库文档数据访问层
 *
 * @author xiaoxin
 * @version v1.0
 * @date 2026/4/11
 */
@Repository
public class KbDocumentInfra extends ServiceImpl<KbDocumentInfra.KbDocumentMapper, KbDocumentEntity> {

    @Mapper
    interface KbDocumentMapper extends BaseMapper<KbDocumentEntity> {}

    // ==================== 单条查询 ====================

    public KbDocumentEntity getDocById(Long id) {
        return this.lambdaQuery().eq(KbDocumentEntity::getId, id).one();
    }

    // ==================== 列表查询 ====================

    public Page<KbDocumentEntity> listByKbId(Long kbId, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(KbDocumentEntity::getKbId, kbId)
                .page(new Page<>(pageNum, pageSize));
    }

    public long countByKbId(Long kbId) {
        return this.lambdaQuery().eq(KbDocumentEntity::getKbId, kbId).count();
    }

    // ==================== 写入 ====================

    public void createDoc(KbDocumentEntity entity) {
        this.save(entity);
    }

    public void updateDoc(KbDocumentEntity entity) {
        this.updateById(entity);
    }

    public void deleteDoc(Long id) {
        this.removeById(id);
    }

    public void deleteByKbId(Long kbId) {
        this.lambdaUpdate().eq(KbDocumentEntity::getKbId, kbId).remove();
    }
}