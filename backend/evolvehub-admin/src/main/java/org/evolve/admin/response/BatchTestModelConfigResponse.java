package org.evolve.admin.response;

import java.util.List;

/**
 * 公共模型批量连通性测试响应
 */
public record BatchTestModelConfigResponse(
        int total,
        int passed,
        int failed,
        List<ModelTestResult> results
) {
    /**
     * 单个模型的测试结果
     */
    public record ModelTestResult(
            Long id,
            String name,
            String provider,
            Integer enabled,
            String status,   // passed / failed / skipped
            String message
    ) {}
}
