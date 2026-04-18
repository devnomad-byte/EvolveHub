package org.evolve.aiplatform.memory;

import org.evolve.aiplatform.memory.domain.bean.dto.MemoryProfileDTO;
import org.evolve.aiplatform.memory.infrastructure.support.MemoryMarkdownRenderer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Memory Markdown 渲染测试
 *
 * @author TellyJiang
 * @since 2026-04-11
 */
class MemoryMarkdownRendererTest {

    @Test
    void shouldRenderDefaultTemplate() {
        MemoryMarkdownRenderer renderer = new MemoryMarkdownRenderer();
        String markdown = renderer.renderDefaultTemplate(1L);
        Assertions.assertTrue(markdown.contains("# 基本信息"));
        Assertions.assertTrue(markdown.contains("# 偏好设置"));
        Assertions.assertTrue(markdown.contains("# 工具设置"));
    }

    @Test
    void shouldRenderStructuredProfileContent() {
        MemoryMarkdownRenderer renderer = new MemoryMarkdownRenderer();
        String markdown = renderer.render(new MemoryProfileDTO(
                1L,
                "张三",
                "研发部",
                "中文",
                "gpt-4o",
                "允许读取文件",
                null
        ));
        Assertions.assertTrue(markdown.contains("张三"));
        Assertions.assertTrue(markdown.contains("研发部"));
        Assertions.assertTrue(markdown.contains("gpt-4o"));
    }
}
