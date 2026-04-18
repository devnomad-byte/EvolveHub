package org.evolve.admin.response;

import java.util.List;

/**
 * S3 Bucket 响应
 */
public class S3BucketResponse {
    private String name;
    private String creationDate;
    private int fileCount;
    private long totalSize;

    public S3BucketResponse() {}

    public S3BucketResponse(String name, String creationDate, int fileCount, long totalSize) {
        this.name = name;
        this.creationDate = creationDate;
        this.fileCount = fileCount;
        this.totalSize = totalSize;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCreationDate() { return creationDate; }
    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }
    public int getFileCount() { return fileCount; }
    public void setFileCount(int fileCount) { this.fileCount = fileCount; }
    public long getTotalSize() { return totalSize; }
    public void setTotalSize(long totalSize) { this.totalSize = totalSize; }
}
