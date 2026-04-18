package org.evolve.admin.service;

import org.evolve.admin.response.S3FileResponse;
import org.evolve.admin.utils.FileStorageUtil;
import org.evolve.common.base.BaseManager;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * S3 文件列表管理
 */
@Service
public class S3FileListManager extends BaseManager<S3FileListManager.Request, List<S3FileResponse>> {

    public record Request(
            String bucketName,
            String path,
            int pageNum,
            int pageSize
    ) {}

    @Resource
    private FileStorageUtil fileStorageUtil;

    @Override
    protected void check(Request request) {
        if (request.bucketName() == null || request.bucketName().isBlank()) {
            throw new BusinessException(ResultCode.BAD_PARAM, "bucketName 不能为空");
        }
    }

    @Override
    protected List<S3FileResponse> process(Request request) {
        String prefix = buildPrefix(request.path());
        FileStorageUtil.ListObjectsResult result = fileStorageUtil.listObjectsWithDelimiter(request.bucketName(), prefix);

        List<S3FileResponse> files = new ArrayList<>();

        // 添加文件夹
        for (String folderPath : result.getFolders()) {
            String folderName = extractFolderName(folderPath, prefix);
            files.add(new S3FileResponse(folderName, folderPath, "folder", 0, null));
        }

        // 添加文件
        for (FileStorageUtil.FileSummary file : result.getFiles()) {
            String fileName = extractFileName(file.getKey(), prefix);
            files.add(new S3FileResponse(
                    fileName,
                    file.getKey(),
                    "file",
                    file.getSize(),
                    file.getLastModified()
            ));
        }

        // 分页
        int start = (request.pageNum() - 1) * request.pageSize();
        int end = Math.min(start + request.pageSize(), files.size());
        if (start >= files.size()) {
            return new ArrayList<>();
        }
        return files.subList(start, end);
    }

    private String buildPrefix(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        if (path.endsWith("/")) {
            return path;
        }
        return path + "/";
    }

    private String extractFolderName(String fullPath, String prefix) {
        if (fullPath.startsWith(prefix)) {
            String relative = fullPath.substring(prefix.length());
            int slashIndex = relative.indexOf('/');
            if (slashIndex > 0) {
                return relative.substring(0, slashIndex);
            }
            return relative;
        }
        return fullPath;
    }

    private String extractFileName(String key, String prefix) {
        if (key.startsWith(prefix)) {
            String relative = key.substring(prefix.length());
            int slashIndex = relative.indexOf('/');
            if (slashIndex > 0) {
                return relative.substring(0, slashIndex);
            }
            return relative;
        }
        return key;
    }
}
