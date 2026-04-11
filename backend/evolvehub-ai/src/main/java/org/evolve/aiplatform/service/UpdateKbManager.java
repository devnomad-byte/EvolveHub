package org.evolve.aiplatform.service;

import jakarta.annotation.Resource;
import org.evolve.aiplatform.bean.dto.request.UpdateKbRequest;
import org.evolve.aiplatform.bean.dto.response.UpdateKbResponse;
import org.evolve.aiplatform.bean.entity.KnowledgeBaseEntity;
import org.evolve.aiplatform.bean.enums.KbLevel;
import org.evolve.aiplatform.mapper.KnowledgeBaseInfra;
import org.evolve.common.base.BaseManager;
import org.evolve.common.infra.DeptInfra;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 更新知识库业务处理器
 *
 * @author xiaoxin
 * @version v1.0
 * @date 2026/4/11
 */
@Service
public class UpdateKbManager extends BaseManager<UpdateKbRequest, UpdateKbResponse> {

    @Resource
    private KnowledgeBaseInfra knowledgeBaseInfra;

    @Resource
    private DeptInfra deptInfra;

    @Override
    protected void check(UpdateKbRequest request) {
        KnowledgeBaseEntity existing = knowledgeBaseInfra.getById(request.id());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "知识库不存在");
        }
        KbLevel level = request.level() != null ? request.level() : existing.getLevel();
        if (level == KbLevel.DEPT || level == KbLevel.PROJECT) {
            Long deptId = request.deptId() != null ? request.deptId() : existing.getDeptId();
            if (deptId == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "DEPT/PROJECT 级知识库必须指定部门ID");
            }
            if (deptInfra.getDeptById(deptId) == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "指定的部门不存在");
            }
        }
    }

    @Override
    protected UpdateKbResponse process(UpdateKbRequest request) {
        KnowledgeBaseEntity entity = new KnowledgeBaseEntity();
        entity.setId(request.id());
        entity.setName(request.name());
        entity.setLevel(request.level());
        entity.setDeptId(request.deptId());
        entity.setDescription(request.description());
        knowledgeBaseInfra.updateKb(entity);
        return new UpdateKbResponse(request.id());
    }
}