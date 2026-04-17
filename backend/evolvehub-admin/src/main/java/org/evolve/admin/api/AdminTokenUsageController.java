package org.evolve.admin.api;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.evolve.common.web.page.PageResponse;
import org.evolve.common.web.response.Result;
import org.evolve.domain.resource.infra.ChatTokenUsageInfra;
import org.evolve.domain.resource.model.ChatTokenUsageEntity;
import org.evolve.domain.rbac.service.UserDataScopeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/token-usage")
public class AdminTokenUsageController {

    @Resource
    private ChatTokenUsageInfra chatTokenUsageInfra;

    @Resource
    private UserDataScopeService userDataScopeService;

    @GetMapping("/records")
    public Result<PageResponse<ChatTokenUsageEntity>> listRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        var dataScopeInfo = userDataScopeService.getDataScopeInfo(currentUserId);
        Page<ChatTokenUsageEntity> page = chatTokenUsageInfra.listPageByDataScope(
                dataScopeInfo.dataScope(), dataScopeInfo.deptId(),
                dataScopeInfo.visibleDeptIds(), currentUserId, pageNum, pageSize);
        return Result.ok(new PageResponse<>(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }
}
