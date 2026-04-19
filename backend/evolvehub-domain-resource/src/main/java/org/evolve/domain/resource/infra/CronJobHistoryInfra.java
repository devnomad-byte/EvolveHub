package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.CronJobHistoryEntity;
import org.springframework.stereotype.Repository;

@Repository
public class CronJobHistoryInfra extends ServiceImpl<CronJobHistoryInfra.CronJobHistoryMapper, CronJobHistoryEntity> {

    @Mapper
    interface CronJobHistoryMapper extends BaseMapper<CronJobHistoryEntity> {}

    public Page<CronJobHistoryEntity> listByJobId(Long jobId, int pageNum, int pageSize) {
        return lambdaQuery()
                .eq(CronJobHistoryEntity::getJobId, jobId)
                .orderByDesc(CronJobHistoryEntity::getStartTime)
                .page(new Page<>(pageNum, pageSize));
    }

    public Page<CronJobHistoryEntity> listPage(int pageNum, int pageSize) {
        return lambdaQuery()
                .orderByDesc(CronJobHistoryEntity::getStartTime)
                .page(new Page<>(pageNum, pageSize));
    }
}
