package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 敏感文件保护全局配置实体
 */
@Data
@TableName("eh_file_guard_config")
public class FileGuardConfigEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 整体开关：1=启用，0=禁用 */
    private Integer enabled;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
