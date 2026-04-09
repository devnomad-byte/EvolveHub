package org.evolve.common.web.page;

import java.util.List;

/**
 * 分页查询响应结果
 * <p>封装分页查询的返回数据，包含当前页记录列表和分页元信息。</p>
 *
 * @param records  当前页的数据记录列表
 * @param total    符合条件的总记录数
 * @param pageNum  当前页码
 * @param pageSize 每页条数
 * @param <T>      记录的实体类型
 * @author zhao
 */
public record PageResponse<T>(
        List<T> records,
        long total,
        int pageNum,
        int pageSize) {
}
