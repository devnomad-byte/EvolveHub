package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.DesktopIconEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DesktopIconInfra extends ServiceImpl<DesktopIconInfra.DesktopIconMapper, DesktopIconEntity> {

    @Mapper
    interface DesktopIconMapper extends BaseMapper<DesktopIconEntity> {}

    public List<DesktopIconEntity> listByCategory(Long categoryId) {
        return lambdaQuery()
                .eq(categoryId != null, DesktopIconEntity::getCategoryId, categoryId)
                .eq(DesktopIconEntity::getDeleted, 0)
                .orderByAsc(DesktopIconEntity::getSort)
                .list();
    }

    public List<DesktopIconEntity> listDesktop() {
        return lambdaQuery()
                .eq(DesktopIconEntity::getIsDesktop, 1)
                .eq(DesktopIconEntity::getDeleted, 0)
                .orderByAsc(DesktopIconEntity::getSort)
                .list();
    }

    public DesktopIconEntity getByPermId(Long permId) {
        return lambdaQuery()
                .eq(DesktopIconEntity::getPermId, permId)
                .eq(DesktopIconEntity::getDeleted, 0)
                .one();
    }

    public List<DesktopIconEntity> listAll() {
        return lambdaQuery()
                .eq(DesktopIconEntity::getDeleted, 0)
                .orderByAsc(DesktopIconEntity::getSort)
                .list();
    }
}
