package org.evolve.admin.service;

import org.evolve.admin.response.AgentRuntimeConfigResponse;
import org.evolve.common.base.BaseManager;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 获取Agent运行时配置默认值
 */
@Service
public class AgentRuntimeConfigDefaultsManager extends BaseManager<Void, List<AgentRuntimeConfigResponse>> {

    @Override
    protected void check(Void request) {}

    @Override
    protected List<AgentRuntimeConfigResponse> process(Void request) {
        // 返回所有配置项的默认值
        return List.of(
                new AgentRuntimeConfigResponse(null, "llm_retry",
                        "{\"llm_retry_enabled\":true,\"llm_max_retries\":3,\"llm_backoff_base\":1.0,\"llm_backoff_cap\":10.0,\"llm_max_concurrent\":10,\"llm_max_qpm\":600,\"llm_rate_limit_pause\":5.0,\"llm_rate_limit_jitter\":1.0,\"llm_acquire_timeout\":120.0}",
                        "LLM重试与限流配置", LocalDateTime.now()),
                new AgentRuntimeConfigResponse(null, "context_compact",
                        "{\"token_count_model\":\"default\",\"token_count_use_mirror\":false,\"token_count_estimate_divisor\":4.0,\"context_compact_enabled\":true,\"memory_compact_ratio\":0.75,\"memory_reserve_ratio\":0.1,\"compact_with_thinking_block\":true}",
                        "上下文压缩配置", LocalDateTime.now()),
                new AgentRuntimeConfigResponse(null, "tool_result_compact",
                        "{\"enabled\":true,\"recent_n\":2,\"old_max_bytes\":3000,\"recent_max_bytes\":50000,\"retention_days\":5}",
                        "工具结果压缩配置", LocalDateTime.now()),
                new AgentRuntimeConfigResponse(null, "memory_summary",
                        "{\"memory_summary_enabled\":true,\"memory_prompt_enabled\":true,\"dream_cron\":\"0 23 * * *\",\"force_memory_search\":false,\"force_max_results\":1,\"force_min_score\":0.3,\"force_memory_search_timeout\":10.0,\"rebuild_memory_index_on_start\":false,\"recursive_file_watcher\":false}",
                        "记忆摘要配置", LocalDateTime.now()),
                new AgentRuntimeConfigResponse(null, "embedding",
                        "{\"backend\":\"openai\",\"api_key\":\"\",\"base_url\":\"\",\"model_name\":\"\",\"dimensions\":1024,\"enable_cache\":true,\"use_dimensions\":false,\"max_cache_size\":3000,\"max_input_length\":8192,\"max_batch_size\":10}",
                        "Embedding模型配置", LocalDateTime.now()),
                new AgentRuntimeConfigResponse(null, "runtime_basic",
                        "{\"max_iters\":100,\"auto_continue_on_text_only\":false,\"max_input_length\":131072,\"history_max_length\":10000}",
                        "基础运行时配置", LocalDateTime.now())
        );
    }
}
