package org.evolve.aiplatform.memory.application.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import org.evolve.aiplatform.memory.domain.bean.dto.MemoryProfileDTO;
import org.evolve.common.base.CurrentUserHolder;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Memory 访问控制测试
 *
 * @author TellyJiang
 * @since 2026-04-11
 */
class MemoryAccessGuardTest {

    @Test
    void shouldRejectWhenCurrentUserIsMissing() {
        CurrentUserHolder.clear();
        MemoryAccessGuard guard = new MemoryAccessGuard(new MemoryOperatorContext());

        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> guard.assertCanAccessUser(1L));

        Assertions.assertEquals(ResultCode.UNAUTHORIZED.getCode(), exception.getCode());
    }

    @Test
    void shouldRejectWhenAccessingOtherUsersMemory() {
        CurrentUserHolder.set(1L);
        try {
            MemoryAccessGuard guard = new MemoryAccessGuard(new MemoryOperatorContext());
            MemoryProfileDTO profile = new MemoryProfileDTO(2L, "李四", "产品部", "中文", "gpt-4o", "读取文件", null);

            BusinessException exception = Assertions.assertThrows(BusinessException.class,
                    () -> guard.assertCanUpdateProfile(profile));

            Assertions.assertEquals(ResultCode.FORBIDDEN.getCode(), exception.getCode());
        } finally {
            CurrentUserHolder.clear();
        }
    }

    @Test
    void shouldAllowAdminToAccessOtherUsersMemory() {
        CurrentUserHolder.set(1L);
        try (MockedStatic<StpUtil> mockedStpUtil = Mockito.mockStatic(StpUtil.class)) {
            mockedStpUtil.when(StpUtil::getRoleList).thenReturn(java.util.List.of("ADMIN"));

            MemoryAccessGuard guard = new MemoryAccessGuard(new MemoryOperatorContext());

            Assertions.assertDoesNotThrow(() -> guard.assertCanAccessUser(2L));
        } finally {
            CurrentUserHolder.clear();
        }
    }
}
