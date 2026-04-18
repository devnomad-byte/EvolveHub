package org.evolve.admin.response;

/**
 * S3 文件响应
 */
public class S3FileResponse {
    private String name;
    private String path;
    private String type;  // "file" 或 "folder"
    private long size;
    private String modifiedTime;

    public S3FileResponse() {}

    public S3FileResponse(String name, String path, String type, long size, String modifiedTime) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.size = size;
        this.modifiedTime = modifiedTime;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public String getModifiedTime() { return modifiedTime; }
    public void setModifiedTime(String modifiedTime) { this.modifiedTime = modifiedTime; }
}
