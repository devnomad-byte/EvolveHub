package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * 安全扫描全局配置实体
 */
@Getter
@Setter
@TableName("eh_security_scanner_config")
public class SecurityScannerConfigEntity extends BaseEntity {

    /** 整体开关（1=启用，0=禁用） */
    private Integer enabled;

    /** 扫描模式：block/warn/off */
    private String mode;

    /** 扫描超时时间（秒） */
    private Integer timeout;
}
