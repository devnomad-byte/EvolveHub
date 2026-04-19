package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.CronJobEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class CronJobInfra extends ServiceImpl<CronJobInfra.CronJobMapper, CronJobEntity> {

    @Mapper
    interface CronJobMapper extends BaseMapper<CronJobEntity> {}

    public List<CronJobEntity> listEnabled() {
        return lambdaQuery()
                .eq(CronJobEntity::getEnabled, 1)
                .eq(CronJobEntity::getDeleted, 0)
                .list();
    }

    public List<CronJobEntity> listByCreateBy(Long createBy) {
        return lambdaQuery()
                .eq(CronJobEntity::getCreateBy, createBy)
                .eq(CronJobEntity::getDeleted, 0)
                .orderByDesc(CronJobEntity::getCreateTime)
                .list();
    }

    public List<CronJobEntity> listByDeptAndChildren(Long deptId) {
        return lambdaQuery()
                .eq(CronJobEntity::getDeptId, deptId)
                .eq(CronJobEntity::getDeleted, 0)
                .orderByDesc(CronJobEntity::getCreateTime)
                .list();
    }

    public List<CronJobEntity> listAllAccessible(Long dataScope, Long userDeptId, List<Long> visibleDeptIds) {
        return lambdaQuery()
                .eq(CronJobEntity::getDeleted, 0)
                .eq(dataScope == 1, CronJobEntity::getDeleted, 0)
                .in(dataScope == 5 && visibleDeptIds != null, CronJobEntity::getDeptId, visibleDeptIds)
                .orderByDesc(CronJobEntity::getCreateTime)
                .list();
    }

    public void updateNextRunTime(Long jobId, LocalDateTime nextRunTime) {
        lambdaUpdate()
                .eq(CronJobEntity::getId, jobId)
                .set(CronJobEntity::getNextRunTime, nextRunTime)
                .update();
    }

    public void updateLastRunStatus(Long jobId, String status, String error) {
        lambdaUpdate()
                .eq(CronJobEntity::getId, jobId)
                .set(CronJobEntity::getLastRunStatus, status)
                .set(CronJobEntity::getLastRunError, error)
                .set(CronJobEntity::getLastRunTime, LocalDateTime.now())
                .update();
    }
}
