package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.ToolGuardHistoryEntity;
import org.springframework.stereotype.Repository;

/**
 * 工具守卫历史数据访问层
 */
@Repository
public class ToolGuardHistoryInfra extends ServiceImpl<ToolGuardHistoryInfra.ToolGuardHistoryMapper, ToolGuardHistoryEntity> {

    @Mapper
    public interface ToolGuardHistoryMapper extends BaseMapper<ToolGuardHistoryEntity> {}

    public Page<ToolGuardHistoryEntity> listPage(String severity, Long userId, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(severity != null && !severity.isEmpty(), ToolGuardHistoryEntity::getSeverity, severity)
                .eq(userId != null, ToolGuardHistoryEntity::getUserId, userId)
                .orderByDesc(ToolGuardHistoryEntity::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
    }

    public void saveHistory(ToolGuardHistoryEntity entity) {
        this.save(entity);
    }

    public void clearAll() {
        this.lambdaUpdate().remove();
    }
}
