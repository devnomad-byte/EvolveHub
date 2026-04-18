package org.evolve.admin.service;

import org.evolve.admin.response.S3BucketResponse;
import org.evolve.admin.utils.FileStorageUtil;
import org.evolve.common.base.BaseManager;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * S3 Bucket 列表管理
 */
@Service
public class S3BucketListManager extends BaseManager<Void, List<S3BucketResponse>> {

    @Resource
    private FileStorageUtil fileStorageUtil;

    @Override
    protected void check(Void request) {}

    @Override
    protected List<S3BucketResponse> process(Void request) {
        List<FileStorageUtil.BucketInfo> buckets = fileStorageUtil.listBuckets();
        return buckets.stream().map(b -> {
            FileStorageUtil.BucketMetadata metadata = fileStorageUtil.getBucketMetadata(b.getName());
            return new S3BucketResponse(
                    b.getName(),
                    b.getCreationDate(),
                    metadata.getFileCount(),
                    metadata.getTotalSize()
            );
        }).toList();
    }
}
