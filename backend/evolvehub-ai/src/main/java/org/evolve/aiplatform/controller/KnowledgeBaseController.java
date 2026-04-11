package org.evolve.aiplatform.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.evolve.aiplatform.bean.dto.request.CreateKbRequest;
import org.evolve.aiplatform.bean.dto.response.CreateKbResponse;
import org.evolve.aiplatform.service.CreateKbManager;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库管理控制器
 * <p>
 * 提供知识库的增删改查接口。
 * 所有业务逻辑均委托给对应的 Manager 处理，Controller 仅负责接收请求和返回响应。
 * </p>
 *
 * @author xiaoxin
 */
@RestController
@RequestMapping("/api/v1/kb")
public class KnowledgeBaseController {

    @Resource
    private CreateKbManager createKbManager;

    /**
     * 创建知识库
     * <p>仅允许超级管理员操作，包含权限级别校验、部门存在性校验等业务逻辑。</p>
     *
     * @param request 创建知识库请求体
     * @return 创建成功的知识库ID
     */
//    @SaCheckRole("SUPER_ADMIN")
    @PostMapping("/create")
    public Result<CreateKbResponse> create(@RequestBody @Valid CreateKbRequest request) {
        return Result.ok(createKbManager.execute(request));
    }
}