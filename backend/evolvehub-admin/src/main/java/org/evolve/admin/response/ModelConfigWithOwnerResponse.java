package org.evolve.admin.response;

import lombok.Getter;
import lombok.Setter;

/**
 * 模型配置（带所有者信息）
 * <p>
 * 用于 SUPER_ADMIN 查看全部模型时，展示每条记录所属的用户昵称
 * </p>
 */
@Getter
@Setter
public class ModelConfigWithOwnerResponse {

    /** 模型配置 ID */
    private Long id;

    /** 模型名称 */
    private String name;

    /** 提供商 */
    private String provider;

    /** API 密钥（脱敏） */
    private String apiKey;

    /** Base URL */
    private String baseUrl;

    /** 是否启用 */
    private Integer enabled;

    /** 模型类型 */
    private String modelType;

    /** 资源范围 */
    private String scope;

    /** 所有者 ID */
    private Long ownerId;

    /** 所有者昵称（SYSTEM 时为 null） */
    private String ownerNickname;

    /** 所有者用户名（SYSTEM 时为 null） */
    private String ownerUsername;
}
