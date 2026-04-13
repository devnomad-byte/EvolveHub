package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.response.ModelConfigWithOwnerResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.rbac.infra.UsersInfra;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.evolve.common.web.page.PageRequest;
import org.evolve.common.web.page.PageResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SUPER_ADMIN 查看全部模型配置（带所有者信息）
 * <p>
 * 返回所有 SYSTEM 和 USER 级模型，左侧可按用户分组查看。
 * </p>
 */
@Service
public class ListModelConfigAdminManager extends BaseManager<PageRequest, PageResponse<ModelConfigWithOwnerResponse>> {

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Resource
    private UsersInfra usersInfra;

    @Override
    protected void check(PageRequest request) {
    }

    @Override
    protected PageResponse<ModelConfigWithOwnerResponse> process(PageRequest request) {
        // 1. 查询全部模型（不分 scope）
        var page = modelConfigInfra.listPage(request.pageNum(), request.pageSize());
        List<ModelConfigEntity> models = page.getRecords();

        // 2. 收集所有 ownerId（排除 SYSTEM 的 null）
        List<Long> ownerIds = models.stream()
                .map(ModelConfigEntity::getOwnerId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        // 3. 批量查询用户信息（使用 final 变量避免 lambda 捕获问题）
        final Map<Long, String> nicknameMap;
        final Map<Long, String> usernameMap;
        if (!ownerIds.isEmpty()) {
            List<org.evolve.domain.rbac.model.UsersEntity> users = new ArrayList<>();
            for (Long ownerId : ownerIds) {
                var user = usersInfra.getUserById(ownerId);
                if (user != null) {
                    users.add(user);
                }
            }
            nicknameMap = users.stream()
                    .filter(u -> u.getNickname() != null)
                    .collect(Collectors.toMap(org.evolve.domain.rbac.model.UsersEntity::getId, org.evolve.domain.rbac.model.UsersEntity::getNickname));
            usernameMap = users.stream()
                    .collect(Collectors.toMap(org.evolve.domain.rbac.model.UsersEntity::getId, org.evolve.domain.rbac.model.UsersEntity::getUsername));
        } else {
            nicknameMap = Map.of();
            usernameMap = Map.of();
        }

        // 4. 组装响应
        List<ModelConfigWithOwnerResponse> records = models.stream().map(m -> {
            ModelConfigWithOwnerResponse r = new ModelConfigWithOwnerResponse();
            r.setId(m.getId());
            r.setName(m.getName());
            r.setProvider(m.getProvider());
            // 脱敏 API Key：只显示前 8 位 + ***
            String rawKey = m.getApiKey();
            r.setApiKey(rawKey != null && rawKey.length() > 8 ? rawKey.substring(0, 8) + "***" : "***");
            r.setBaseUrl(m.getBaseUrl());
            r.setEnabled(m.getEnabled());
            r.setModelType(m.getModelType());
            r.setScope(m.getScope());
            r.setOwnerId(m.getOwnerId());
            if (m.getOwnerId() != null) {
                r.setOwnerNickname(nicknameMap.getOrDefault(m.getOwnerId(), "未知用户"));
                r.setOwnerUsername(usernameMap.getOrDefault(m.getOwnerId(), ""));
            }
            return r;
        }).toList();

        return new PageResponse<>(records, page.getTotal(), request.pageNum(), request.pageSize());
    }
}
