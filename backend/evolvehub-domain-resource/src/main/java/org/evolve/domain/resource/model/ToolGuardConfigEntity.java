package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 工具守卫全局配置实体
 */
@Getter
@Setter
@TableName("eh_tool_guard_config")
public class ToolGuardConfigEntity {

    /** 固定为1 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 整体开关（1=启用，0=禁用） */
    private Integer enabled;

    /** 受保护的工具列表，JSON数组，null表示全部 */
    private String guardedTools;

    /** 直接拒绝的工具列表，JSON数组 */
    private String deniedTools;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
