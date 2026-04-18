package org.evolve.aiplatform.memory.framework.agentscope;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Memory 运行时 JSON 编解码器
 *
 * @author TellyJiang
 * @since 2026-04-12
 */
@Component
public class MemoryRuntimeJsonCodec {

    private final ObjectMapper objectMapper;

    /**
     * 构造 JSON 编解码器
     *
     * @param objectMapper Jackson 对象映射器
     * @author TellyJiang
     * @since 2026-04-12
     */
    public MemoryRuntimeJsonCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy().findAndRegisterModules();
    }

    /**
     * 序列化对象
     *
     * @param value 待序列化对象
     * @return JSON 字符串
     * @author TellyJiang
     * @since 2026-04-12
     */
    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.FAIL, "Memory 运行时序列化失败");
        }
    }

    /**
     * 反序列化对象
     *
     * @param json JSON 内容
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 目标对象
     * @author TellyJiang
     * @since 2026-04-12
     */
    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.FAIL, "Memory 运行时反序列化失败");
        }
    }

    /**
     * 反序列化列表
     *
     * @param json JSON 内容
     * @param clazz 元素类型
     * @param <T> 泛型类型
     * @return 列表结果
     * @author TellyJiang
     * @since 2026-04-12
     */
    public <T> List<T> convertList(String json, Class<T> clazz) {
        try {
            JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.FAIL, "Memory 运行时列表反序列化失败");
        }
    }
}
