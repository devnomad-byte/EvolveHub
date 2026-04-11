package org.evolve.aiplatform.service;

import jakarta.annotation.Resource;
import org.evolve.aiplatform.bean.dto.request.UploadDocRequest;
import org.evolve.aiplatform.bean.dto.response.UploadDocResponse;
import org.evolve.aiplatform.bean.entity.KbDocumentEntity;
import org.evolve.aiplatform.mapper.KbDocumentInfra;
import org.evolve.aiplatform.mapper.KnowledgeBaseInfra;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 上传文档业务处理器
 * <p>
 * 业务规则：
 * <ul>
 *     <li>所属知识库必须存在</li>
 *     <li>文档初始状态为待处理（status=0）</li>
 * </ul>
 *
 * @author xiaoxin
 * @version v1.0
 * @date 2026/4/11
 */
@Service
public class UploadDocManager extends BaseManager<UploadDocRequest, UploadDocResponse> {

    @Resource
    private KnowledgeBaseInfra knowledgeBaseInfra;

    @Resource
    private KbDocumentInfra kbDocumentInfra;

    @Override
    protected void check(UploadDocRequest request) {
        if (knowledgeBaseInfra.getById(request.kbId()) == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "知识库不存在");
        }
    }

    @Override
    protected UploadDocResponse process(UploadDocRequest request) {
        KbDocumentEntity entity = new KbDocumentEntity();
        entity.setKbId(request.kbId());
        entity.setFileName(request.fileName());
        entity.setFilePath(request.filePath());
        entity.setFileSize(request.fileSize());
        entity.setChunkCount(0);
        entity.setStatus(0);
        kbDocumentInfra.createDoc(entity);
        return new UploadDocResponse(entity.getId());
    }
}