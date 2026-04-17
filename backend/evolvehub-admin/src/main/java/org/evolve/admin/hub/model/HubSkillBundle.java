package org.evolve.admin.hub.model;

import java.util.List;
import java.util.Map;

public class HubSkillBundle {
    private String name;
    private String version;
    private String content;  // SKILL.md 内容
    private String readme;    // README.md 内容
    private List<String> tags;
    private String sourceUrl;
    private byte[] zipData;   // 完整 zip 包数据
    private Map<String, byte[]> files;  // 所有文件 <filename, content>

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getReadme() { return readme; }
    public void setReadme(String readme) { this.readme = readme; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public byte[] getZipData() { return zipData; }
    public void setZipData(byte[] zipData) { this.zipData = zipData; }
    public Map<String, byte[]> getFiles() { return files; }
    public void setFiles(Map<String, byte[]> files) { this.files = files; }
}
