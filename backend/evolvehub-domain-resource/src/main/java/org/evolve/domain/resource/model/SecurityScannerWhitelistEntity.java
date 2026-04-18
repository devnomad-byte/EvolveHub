package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * 安全扫描白名单实体
 */
@Getter
@Setter
@TableName("eh_security_scanner_whitelist")
public class SecurityScannerWhitelistEntity extends BaseEntity {

    /** 技能名称 */
    private String skillName;

    /** 内容哈希（SHA-256） */
    private String contentHash;

    /** 添加人 */
    private Long addedBy;
}
