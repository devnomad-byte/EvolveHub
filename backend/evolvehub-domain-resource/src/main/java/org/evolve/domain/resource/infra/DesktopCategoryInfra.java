package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.DesktopCategoryEntity;
import org.springframework.stereotype.Repository;

@Repository
public class DesktopCategoryInfra extends ServiceImpl<DesktopCategoryInfra.DesktopCategoryMapper, DesktopCategoryEntity> {

    @Mapper
    interface DesktopCategoryMapper extends BaseMapper<DesktopCategoryEntity> {}
}
