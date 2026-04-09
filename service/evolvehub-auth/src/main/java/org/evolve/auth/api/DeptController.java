package org.evolve.auth.api;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.evolve.auth.user.request.CreateDeptRequest;
import org.evolve.auth.user.response.CreateDeptResponse;
import org.evolve.auth.user.service.CreateDeptManager;
import org.evolve.common.web.response.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dept")
@RequiredArgsConstructor
public class DeptController {

    @Resource
    private CreateDeptManager createDeptManager;

    @PostMapping("/create")
    public Result<CreateDeptResponse> createDept(@RequestBody @Valid CreateDeptRequest request) {
        return Result.ok(createDeptManager.execute(request));
    }
}
