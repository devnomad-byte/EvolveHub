package org.evolve.admin.response;

import java.util.List;

public class FileNode {
    private String name;
    private String path;
    private String type;  // "file" 或 "folder"
    private long size;
    private String modifiedTime;
    private List<FileNode> children;

    public FileNode() {}

    public FileNode(String name, String path, String type, long size, String modifiedTime) {
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
    public List<FileNode> getChildren() { return children; }
    public void setChildren(List<FileNode> children) { this.children = children; }

    public static FileNodeBuilder builder() {
        return new FileNodeBuilder();
    }

    public static class FileNodeBuilder {
        private String name;
        private String path;
        private String type;
        private long size;
        private String modifiedTime;
        private List<FileNode> children;

        public FileNodeBuilder name(String name) { this.name = name; return this; }
        public FileNodeBuilder path(String path) { this.path = path; return this; }
        public FileNodeBuilder type(String type) { this.type = type; return this; }
        public FileNodeBuilder size(long size) { this.size = size; return this; }
        public FileNodeBuilder modifiedTime(String modifiedTime) { this.modifiedTime = modifiedTime; return this; }
        public FileNodeBuilder children(List<FileNode> children) { this.children = children; return this; }
        public FileNode build() {
            FileNode node = new FileNode(name, path, type, size, modifiedTime);
            node.setChildren(children);
            return node;
        }
    }
}
