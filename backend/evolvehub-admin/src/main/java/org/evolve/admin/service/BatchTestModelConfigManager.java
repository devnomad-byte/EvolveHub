package org.evolve.admin.service;

import jakarta.annotation.Resource;
import org.evolve.admin.response.BatchTestModelConfigResponse;
import org.evolve.admin.response.BatchTestModelConfigResponse.ModelTestResult;
import org.evolve.common.base.BaseManager;
import org.evolve.domain.resource.ai.ModelConnectivityTester;
import org.evolve.domain.resource.infra.ModelConfigInfra;
import org.evolve.domain.resource.model.ModelConfigEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量测试 SYSTEM 公共模型连通性
 * <p>
 * 流程：
 * <ul>
 *     <li>查询所有 scope=SYSTEM 的模型</li>
 *     <li>逐个调用 ModelConnectivityTester 测试</li>
 *     <li>失败模型自动禁用（enabled=0）</li>
 *     <li>返回每个模型的测试结果</li>
 * </ul>
 */
@Service
public class BatchTestModelConfigManager extends BaseManager<Void, BatchTestModelConfigResponse> {

    @Resource
    private ModelConfigInfra modelConfigInfra;

    @Resource
    private ModelConnectivityTester modelConnectivityTester;

    @Override
    protected void check(Void request) {}

    @Override
    protected BatchTestModelConfigResponse process(Void request) {
        // 1. 查询所有 SYSTEM 模型
        List<ModelConfigEntity> systemModels = modelConfigInfra.listSystemModels();

        List<ModelTestResult> results = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>();

        for (ModelConfigEntity model : systemModels) {
            // 已禁用的跳过测试
            if (model.getEnabled() != null && model.getEnabled() == 0) {
                results.add(new ModelTestResult(
                        model.getId(),
                        model.getName(),
                        model.getProvider(),
                        model.getEnabled(),
                        "skipped",
                        "已处于禁用状态，跳过测试"
                ));
                continue;
            }

            // 测试连通性
            ModelConnectivityTester.TestResult testResult = modelConnectivityTester.test(
                    model.getProvider(),
                    model.getBaseUrl(),
                    model.getApiKey()
            );

            if (testResult.success()) {
                results.add(new ModelTestResult(
                        model.getId(),
                        model.getName(),
                        model.getProvider(),
                        model.getEnabled(),
                        "passed",
                        "连通性测试通过"
                ));
            } else {
                // 失败，记录 ID，稍后批量更新 enabled=0
                failedIds.add(model.getId());
                results.add(new ModelTestResult(
                        model.getId(),
                        model.getName(),
                        model.getProvider(),
                        0,
                        "failed",
                        testResult.message()
                ));
            }
        }

        // 2. 批量禁用失败模型
        if (!failedIds.isEmpty()) {
            modelConfigInfra.lambdaUpdate()
                    .in(ModelConfigEntity::getId, failedIds)
                    .set(ModelConfigEntity::getEnabled, 0)
                    .update();
        }

        long passed = results.stream().filter(r -> "passed".equals(r.status())).count();
        long failed = results.stream().filter(r -> "failed".equals(r.status())).count();

        return new BatchTestModelConfigResponse(
                results.size(),
                (int) passed,
                (int) failed,
                results
        );
    }
}
