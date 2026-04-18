package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * 安全扫描阻断历史实体
 */
@Getter
@Setter
@TableName("eh_security_scanner_blocked_history")
public class SecurityScannerBlockedHistoryEntity extends BaseEntity {

    /** 技能名称 */
    private String skillName;

    /** 操作类型：BLOCKED/WARNED */
    private String action;

    /** 文件内容哈希 */
    private String contentHash;

    /** 最高严重级别 */
    private String maxSeverity;

    /** 发现的问题详情，JSON数组 */
    private String findings;

    /** 操作用户 */
    private Long userId;

    /** 操作用户昵称 */
    private String userNickname;
}
