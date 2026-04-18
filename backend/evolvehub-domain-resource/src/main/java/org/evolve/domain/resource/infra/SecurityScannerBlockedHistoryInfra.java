package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.SecurityScannerBlockedHistoryEntity;
import org.springframework.stereotype.Repository;

/**
 * 安全扫描阻断历史数据访问层
 */
@Repository
public class SecurityScannerBlockedHistoryInfra extends ServiceImpl<SecurityScannerBlockedHistoryInfra.SecurityScannerBlockedHistoryMapper, SecurityScannerBlockedHistoryEntity> {

    @Mapper
    public interface SecurityScannerBlockedHistoryMapper extends BaseMapper<SecurityScannerBlockedHistoryEntity> {}

    public Page<SecurityScannerBlockedHistoryEntity> listPage(int pageNum, int pageSize, String action, Long userId) {
        return this.lambdaQuery()
                .eq(SecurityScannerBlockedHistoryEntity::getDeleted, 0)
                .eq(action != null, SecurityScannerBlockedHistoryEntity::getAction, action)
                .eq(userId != null, SecurityScannerBlockedHistoryEntity::getUserId, userId)
                .orderByDesc(SecurityScannerBlockedHistoryEntity::getCreateTime)
                .page(new Page<>(pageNum, pageSize));
    }

    public SecurityScannerBlockedHistoryEntity getById(Long id) {
        return this.lambdaQuery()
                .eq(SecurityScannerBlockedHistoryEntity::getDeleted, 0)
                .eq(SecurityScannerBlockedHistoryEntity::getId, id)
                .one();
    }

    public void deleteAll() {
        this.lambdaUpdate()
                .set(SecurityScannerBlockedHistoryEntity::getDeleted, 1)
                .eq(SecurityScannerBlockedHistoryEntity::getDeleted, 0)
                .update();
    }
}
