package org.evolve.auth.user.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * @className DeptEntity
 * @description 部门实体类（树形结构），用于组织架构与数据权限
 * @author zhao
 * @date 2026/4/9
 * @version v1.0
 **/
@Getter
@Setter
@TableName("t_dept")
public class DeptEntity extends BaseEntity {

    /**
     * 父部门 ID（0 表示顶级部门）
     */
    private Long parentId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 状态（0-禁用 1-正常）
     */
    private Integer status;
}
