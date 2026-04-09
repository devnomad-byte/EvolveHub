package org.evolve.common.web.page;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 分页查询请求参数
 * <p>封装分页查询所需的页码和每页条数，并提供默认值和边界校验。</p>
 *
 * @param pageNum  页码，最小为1，默认为1
 * @param pageSize 每页条数，范围为1-100，默认为10
 * @author zhao
 */
public record PageRequest(
        @Min(value = 1, message = "页码最小为1")
        int pageNum,
        @Min(value = 1, message = "每页条数最小为1")
        @Max(value = 100, message = "每页条数最大为100")
        int pageSize) {

    /**
     * 紧凑构造器：当传入值为0时自动设置默认值
     */
    public PageRequest {
        if (pageNum == 0) pageNum = 1;
        if (pageSize == 0) pageSize = 10;
    }
}
