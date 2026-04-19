package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.evolve.common.base.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("eh_desktop_icon")
public class DesktopIconEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long permId;

    private Long categoryId;

    private Integer isDesktop;

    private Integer sort;

    private Long createBy;
}
