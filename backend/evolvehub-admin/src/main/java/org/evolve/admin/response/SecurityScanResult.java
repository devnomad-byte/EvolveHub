package org.evolve.admin.response;

import lombok.Data;

import java.util.List;

/**
 * 安全扫描结果
 */
@Data
public class SecurityScanResult {

    private boolean passed;

    /** 最高严重程度：CRITICAL / HIGH / MEDIUM / LOW */
    private String maxSeverity;

    /** 所有发现的问题 */
    private List<SecurityFinding> findings;

    public static SecurityScanResult pass() {
        SecurityScanResult r = new SecurityScanResult();
        r.setPassed(true);
        return r;
    }

    public static SecurityScanResult fail(List<SecurityFinding> findings) {
        SecurityScanResult r = new SecurityScanResult();
        r.setPassed(false);
        r.setFindings(findings);
        r.setMaxSeverity(calculateMaxSeverity(findings));
        return r;
    }

    private static String calculateMaxSeverity(List<SecurityFinding> findings) {
        for (String sev : new String[]{"CRITICAL", "HIGH", "MEDIUM", "LOW"}) {
            for (SecurityFinding f : findings) {
                if (sev.equals(f.getSeverity())) return sev;
            }
        }
        return "LOW";
    }
}
