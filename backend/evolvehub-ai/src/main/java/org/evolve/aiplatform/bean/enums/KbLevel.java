package org.evolve.aiplatform.bean.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 知识库权限级别
 *
 * <ul>
 *     <li>GLOBAL  (1) - 全公司可见，不限部门</li>
 *     <li>DEPT    (2) - 部门级，仅所属部门可见，deptId 必填</li>
 *     <li>PROJECT (3) - 项目级，仅项目所属部门可见，deptId 必填</li>
 *     <li>SENSITIVE(4) - 敏感级，仅创建者可见</li>
 * </ul>
 *
 * @author xiaoxin
 * @version v1.0
 * @date 2026/4/11
 */
public enum KbLevel {

    GLOBAL(1),
    DEPT(2),
    PROJECT(3),
    SENSITIVE(4);

    @EnumValue
    private final int value;

    KbLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}