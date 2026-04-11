package org.evolve.auth.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门信息响应
 *
 * @author zhao
 */
public record DeptResponse(

        /**
         * 部门 ID
         */
        Long id,

        /**
         * 部门名称
         */
        String deptName,

        /**
         * 上级部门 ID
         */
        Long parentId,

        /**
         * 排序号
         */
        Integer sort,

        /**
         * 状态（0-禁用 1-正常）
         */
        Integer status,

        /**
         * 创建时间
         */
        LocalDateTime createTime,

        /**
         * 更新时间
         */
        LocalDateTime updateTime,

        /**
         * 子部门列表
         */
        List<DeptResponse> children
) {
}
