package org.evolve.auth.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.evolve.auth.user.request.CreateDeptRequest;
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

    private final CreateDeptManager createDeptManager;

    @PostMapping("/create")
    public Result<Void> createDept(@RequestBody @Valid CreateDeptRequest request) {
        createDeptManager.create(request);
        return Result.ok();
    }
}
