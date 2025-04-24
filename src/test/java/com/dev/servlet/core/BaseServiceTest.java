package com.dev.servlet.core;

import com.dev.servlet.core.util.CacheUtils;
import com.dev.servlet.core.util.PropertiesUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

public abstract class BaseServiceTest {
    protected static final String TEST_TOKEN = "test-token-with-sufficient-length-for-cache";

    protected static MockedStatic<PropertiesUtil> propertiesUtilMock;
    protected static MockedStatic<CacheUtils> cacheUtilsMock;

    @BeforeAll
    static void setUpBaseClass() {
        propertiesUtilMock = mockStatic(PropertiesUtil.class);
        propertiesUtilMock.when(() -> PropertiesUtil.getProperty(anyString())).thenReturn("1");
        propertiesUtilMock.when(() -> PropertiesUtil.getProperty(anyString(), any())).thenReturn(1L);

        cacheUtilsMock = mockStatic(CacheUtils.class);
        cacheUtilsMock
                .when(() -> CacheUtils.clearCacheKeyPrefix(anyString(), eq(TEST_TOKEN)))
                .thenAnswer(invocation -> null);
    }

    @AfterAll
    static void tearDownBaseClass() {
        if (propertiesUtilMock != null) {
            propertiesUtilMock.close();
        }

        if (cacheUtilsMock != null) {
            cacheUtilsMock.close();
        }
    }
}