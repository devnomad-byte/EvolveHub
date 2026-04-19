package org.evolve.domain.resource.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.evolve.common.base.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("eh_desktop_category")
public class DesktopCategoryEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String icon;

    private String color;

    private Integer sort;

    private Integer status;

    private Long createBy;
}
