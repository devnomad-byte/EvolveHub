package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

import java.time.LocalDateTime;

/**
 * 用户 API Key 实体类
 * <p>
 * 一个用户对应一个 API Key，用于调用系统级资源时的身份认证。
 * </p>
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/13
 */
@Getter
@Setter
@TableName("eh_api_key")
public class ApiKeyEntity extends BaseEntity {

    /**
     * 绑定的用户 ID（一对一）
     */
    private Long userId;

    /**
     * 密钥值，格式：sk-{32位随机hex}
     */
    private String apiKey;

    /**
     * 状态：1-正常 0-禁用
     */
    private Integer status;

    /**
     * 过期时间，NULL 表示永不过期
     */
    private LocalDateTime expiredAt;
}
