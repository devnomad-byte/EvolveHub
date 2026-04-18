package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.FileGuardConfigEntity;
import org.springframework.stereotype.Repository;

/**
 * 敏感文件保护配置数据访问层
 */
@Repository
public class FileGuardConfigInfra extends ServiceImpl<FileGuardConfigInfra.FileGuardConfigMapper, FileGuardConfigEntity> {

    @Mapper
    interface FileGuardConfigMapper extends BaseMapper<FileGuardConfigEntity> {}

    /**
     * 获取配置（单条）
     */
    public FileGuardConfigEntity getConfig() {
        return lambdaQuery()
                .eq(FileGuardConfigEntity::getId, 1L)
                .one();
    }
}
