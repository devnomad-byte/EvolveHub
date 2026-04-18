package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.SecurityScannerConfigEntity;
import org.springframework.stereotype.Repository;

/**
 * 安全扫描配置数据访问层
 */
@Repository
public class SecurityScannerConfigInfra extends ServiceImpl<SecurityScannerConfigInfra.SecurityScannerConfigMapper, SecurityScannerConfigEntity> {

    @Mapper
    public interface SecurityScannerConfigMapper extends BaseMapper<SecurityScannerConfigEntity> {}

    public SecurityScannerConfigEntity getConfig() {
        return this.lambdaQuery()
                .eq(SecurityScannerConfigEntity::getDeleted, 0)
                .one();
    }
}
