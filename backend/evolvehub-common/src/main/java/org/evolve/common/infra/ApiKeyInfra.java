package org.evolve.common.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.common.model.ApiKeyEntity;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * API Key 数据访问层
 *
 * @author zhao
 * @version v1.0
 * @date 2026/4/13
 */
@Repository
public class ApiKeyInfra extends ServiceImpl<ApiKeyInfra.ApiKeyMapper, ApiKeyEntity> {

    @Mapper
    interface ApiKeyMapper extends BaseMapper<ApiKeyEntity> {}

    /**
     * 根据密钥值查询
     *
     * @param apiKey 密钥值
     * @return API Key 实体，不存在返回 null
     */
    public ApiKeyEntity getByApiKey(String apiKey) {
        return this.lambdaQuery().eq(ApiKeyEntity::getApiKey, apiKey).one();
    }

    /**
     * 根据用户 ID 查询
     *
     * @param userId 用户 ID
     * @return API Key 实体，不存在返回 null
     */
    public ApiKeyEntity getByUserId(Long userId) {
        return this.lambdaQuery().eq(ApiKeyEntity::getUserId, userId).one();
    }

    /**
     * 为用户创建 API Key
     *
     * @param userId 用户 ID
     * @return 创建的 API Key 实体
     */
    public ApiKeyEntity createForUser(Long userId) {
        ApiKeyEntity entity = new ApiKeyEntity();
        entity.setUserId(userId);
        entity.setApiKey(generateApiKey());
        entity.setStatus(1);
        this.save(entity);
        return entity;
    }

    /**
     * 重置用户的 API Key（生成新密钥）
     *
     * @param userId 用户 ID
     * @return 新的 API Key 实体
     */
    public ApiKeyEntity resetForUser(Long userId) {
        ApiKeyEntity existing = getByUserId(userId);
        if (existing != null) {
            existing.setApiKey(generateApiKey());
            this.updateById(existing);
            return existing;
        }
        return createForUser(userId);
    }

    /**
     * 禁用用户的 API Key
     *
     * @param userId 用户 ID
     */
    public void disableByUserId(Long userId) {
        ApiKeyEntity existing = getByUserId(userId);
        if (existing != null) {
            existing.setStatus(0);
            this.updateById(existing);
        }
    }

    /**
     * 生成 sk- 前缀的 API Key
     *
     * @return 格式为 sk-{32位随机hex} 的密钥
     */
    private String generateApiKey() {
        return "sk-" + UUID.randomUUID().toString().replace("-", "");
    }
}
