package org.evolve.admin.hub.model;

import java.util.List;

public class HubSearchResult {
    private String name;
    private String description;
    private String author;
    private String hubName;
    private String bundleUrl;
    private List<String> tags;
    private int downloads;
    private String version;
    private String sourceUrl;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getHubName() { return hubName; }
    public void setHubName(String hubName) { this.hubName = hubName; }
    public String getBundleUrl() { return bundleUrl; }
    public void setBundleUrl(String bundleUrl) { this.bundleUrl = bundleUrl; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public int getDownloads() { return downloads; }
    public void setDownloads(int downloads) { this.downloads = downloads; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
}
