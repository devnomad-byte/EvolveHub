package org.evolve.admin.response;

import java.util.List;

public record DesktopAllResponse(
        List<CategoryResponse> categories,
        List<IconResponse> icons,
        List<IconResponse> desktopIcons
) {}
