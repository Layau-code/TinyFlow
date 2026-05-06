package com.layor.tinyflow.Strategy;

import org.hashids.Hashids;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Hashids短码生成策略测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Hashids策略测试")
class HashidsStrategyTest {

    @Mock
    private Hashids hashids;

    @InjectMocks
    private HashidsStrategy hashidsStrategy;

    @Test
    @DisplayName("编码ID - 成功")
    void testEncode_Success() {
        // Given
        long testId = 12345L;
        String expectedCode = "abc123";
        when(hashids.encode(testId)).thenReturn(expectedCode);

        // When
        String result = hashidsStrategy.encode(testId);

        // Then
        assertEquals(expectedCode, result);
        verify(hashids, times(1)).encode(testId);
    }

    @Test
    @DisplayName("编码不同ID - 生成不同短码")
    void testEncode_DifferentIds() {
        // Given
        long id1 = 123L;
        long id2 = 456L;
        String code1 = "abc";
        String code2 = "def";
        
        when(hashids.encode(id1)).thenReturn(code1);
        when(hashids.encode(id2)).thenReturn(code2);

        // When
        String result1 = hashidsStrategy.encode(id1);
        String result2 = hashidsStrategy.encode(id2);

        // Then
        assertNotEquals(result1, result2);
        assertEquals(code1, result1);
        assertEquals(code2, result2);
    }
}
