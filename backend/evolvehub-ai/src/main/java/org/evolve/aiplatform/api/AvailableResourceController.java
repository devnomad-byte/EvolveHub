package org.evolve.aiplatform.api;

import jakarta.annotation.Resource;
import org.evolve.aiplatform.service.ListAvailableMcpsManager;
import org.evolve.aiplatform.service.ListAvailableModelsManager;
import org.evolve.aiplatform.service.ListAvailableSkillsManager;
import org.evolve.domain.resource.model.McpConfigEntity;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.evolve.domain.resource.model.SkillConfigEntity;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 可用资源聚合查询控制器
 * <p>
 * 返回当前用户可使用的所有资源（自己的用户级资源 + 被授权的系统级资源）。
 * </p>
 *
 * @author zhao
 */
@RestController
@RequestMapping("/available")
public class AvailableResourceController {

    @Resource
    private ListAvailableModelsManager listAvailableModelsManager;

    @Resource
    private ListAvailableSkillsManager listAvailableSkillsManager;

    @Resource
    private ListAvailableMcpsManager listAvailableMcpsManager;

    /**
     * 查询当前用户可用的模型列表
     */
    @GetMapping("/models")
    public Result<List<ModelConfigEntity>> availableModels() {
        return Result.ok(listAvailableModelsManager.execute());
    }

    /**
     * 查询当前用户可用的技能列表
     */
    @GetMapping("/skills")
    public Result<List<SkillConfigEntity>> availableSkills() {
        return Result.ok(listAvailableSkillsManager.execute());
    }

    /**
     * 查询当前用户可用的 MCP 服务列表
     */
    @GetMapping("/mcps")
    public Result<List<McpConfigEntity>> availableMcps() {
        return Result.ok(listAvailableMcpsManager.execute());
    }
}
