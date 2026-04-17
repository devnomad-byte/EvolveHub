package org.evolve.admin.request;

public record InstallSkillFromHubRequest(
    String hubName,
    String bundleUrl,
    Boolean skipScan
) {
    public boolean shouldSkipScan() {
        return skipScan != null && skipScan;
    }
}
