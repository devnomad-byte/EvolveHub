package org.evolve.aiplatform.service;

import jakarta.annotation.Resource;
import org.evolve.aiplatform.bean.dto.request.CreateKbRequest;
import org.evolve.aiplatform.bean.dto.response.CreateKbResponse;
import org.evolve.aiplatform.bean.entity.KnowledgeBaseEntity;
import org.evolve.aiplatform.bean.enums.KbLevel;
import org.evolve.aiplatform.mapper.KnowledgeBaseInfra;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.DeptInfra;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 创建知识库业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>level 为 DEPT 或 PROJECT 时 deptId 必须传值</li>
 *     <li>deptId 必须对应数据库中真实存在的部门</li>
 * </ul>
 *
 * @author xiaoxin
 * @version v1.0
 * @date 2026/4/11
 */
@Service
public class CreateKbManager extends BaseManager<CreateKbRequest, CreateKbResponse> {

    @Resource
    private KnowledgeBaseInfra knowledgeBaseInfra;

    @Resource
    private DeptInfra deptInfra;

    @Override
    protected void check(CreateKbRequest request) {
        KbLevel level = request.level();
        if (level == KbLevel.DEPT || level == KbLevel.PROJECT) {
            if (request.deptId() == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "DEPT/PROJECT 级知识库必须指定部门ID");
            }
            if (deptInfra.getDeptById(request.deptId()) == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "指定的部门不存在");
            }
        }
    }

    @Override
    protected CreateKbResponse process(CreateKbRequest request) {
        KnowledgeBaseEntity entity = new KnowledgeBaseEntity();
        entity.setName(request.name());
        entity.setLevel(request.level());
        entity.setDeptId(request.deptId());
        entity.setOwnerId(request.ownerId());
        entity.setDescription(request.description());
        knowledgeBaseInfra.createKb(entity);
        return new CreateKbResponse(entity.getId());
    }
}