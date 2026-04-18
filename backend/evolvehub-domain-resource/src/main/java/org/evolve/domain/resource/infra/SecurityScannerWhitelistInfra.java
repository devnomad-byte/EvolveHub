package org.evolve.domain.resource.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.domain.resource.model.SecurityScannerWhitelistEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 安全扫描白名单数据访问层
 */
@Repository
public class SecurityScannerWhitelistInfra extends ServiceImpl<SecurityScannerWhitelistInfra.SecurityScannerWhitelistMapper, SecurityScannerWhitelistEntity> {

    @Mapper
    public interface SecurityScannerWhitelistMapper extends BaseMapper<SecurityScannerWhitelistEntity> {}

    public SecurityScannerWhitelistEntity getById(Long id) {
        return this.lambdaQuery()
                .eq(SecurityScannerWhitelistEntity::getDeleted, 0)
                .eq(SecurityScannerWhitelistEntity::getId, id)
                .one();
    }

    public List<SecurityScannerWhitelistEntity> listAll() {
        return this.lambdaQuery()
                .eq(SecurityScannerWhitelistEntity::getDeleted, 0)
                .orderByDesc(SecurityScannerWhitelistEntity::getCreateTime)
                .list();
    }

    public List<SecurityScannerWhitelistEntity> listBySkillName(String skillName) {
        return this.lambdaQuery()
                .eq(SecurityScannerWhitelistEntity::getDeleted, 0)
                .eq(SecurityScannerWhitelistEntity::getSkillName, skillName)
                .list();
    }

    public boolean existsByHash(String contentHash) {
        return this.lambdaQuery()
                .eq(SecurityScannerWhitelistEntity::getDeleted, 0)
                .eq(SecurityScannerWhitelistEntity::getContentHash, contentHash)
                .count() > 0;
    }

    public boolean existsBySkillNameAndHash(String skillName, String contentHash) {
        return this.lambdaQuery()
                .eq(SecurityScannerWhitelistEntity::getDeleted, 0)
                .eq(SecurityScannerWhitelistEntity::getSkillName, skillName)
                .eq(SecurityScannerWhitelistEntity::getContentHash, contentHash)
                .count() > 0;
    }
}
