package org.evolve.auth.service;

import jakarta.annotation.Resource;
import org.evolve.auth.response.DeptResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.DeptInfra;
import org.evolve.common.model.DeptEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 查询部门树业务处理器
 *
 * @author zhao
 */
@Service
public class DeptTreeManager extends BaseManager<Void, List<DeptResponse>> {

    @Resource
    private DeptInfra deptInfra;

    @Override
    protected void check(Void request) {
        // 无需校验
    }

    @Override
    protected List<DeptResponse> process(Void request) {
        // 查询所有部门
        List<DeptEntity> allDepts = deptInfra.list();

        // 转换为响应对象
        List<DeptResponse> deptResponses = allDepts.stream()
                .map(this::buildDeptResponse)
                .collect(Collectors.toList());

        // 构建树形结构
        return buildTree(deptResponses, 0L);
    }

    private DeptResponse buildDeptResponse(DeptEntity dept) {
        return new DeptResponse(
                dept.getId(),
                dept.getDeptName(),
                dept.getParentId(),
                dept.getSort(),
                dept.getStatus(),
                dept.getCreateTime(),
                dept.getUpdateTime(),
                new ArrayList<>() // 子部门列表，稍后填充
        );
    }

    /**
     * 递归构建树形结构
     *
     * @param allDepts 所有部门列表
     * @param parentId 父部门 ID
     * @return 树形结构
     */
    private List<DeptResponse> buildTree(List<DeptResponse> allDepts, Long parentId) {
        List<DeptResponse> tree = new ArrayList<>();

        for (DeptResponse dept : allDepts) {
            if (dept.parentId().equals(parentId)) {
                // 递归查找子部门
                List<DeptResponse> children = buildTree(allDepts, dept.id());
                dept = new DeptResponse(
                        dept.id(),
                        dept.deptName(),
                        dept.parentId(),
                        dept.sort(),
                        dept.status(),
                        dept.createTime(),
                        dept.updateTime(),
                        children
                );
                tree.add(dept);
            }
        }

        return tree;
    }
}
