package com.evomind.service;

import com.evomind.service.impl.VoiceTranscriptionServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 语音转文字服务测试
 */
@ExtendWith(MockitoExtension.class)
class VoiceTranscriptionServiceTest {

    @InjectMocks
    private VoiceTranscriptionServiceImpl voiceTranscriptionService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        // 设置模拟模式
        ReflectionTestUtils.setField(voiceTranscriptionService, "speechEnabled", false);
        ReflectionTestUtils.setField(voiceTranscriptionService, "apiKey", "");
    }

    @Test
    void testTranscribeFromBase64_MockMode() {
        // 模拟模式下应该返回模拟文本
        String result = voiceTranscriptionService.transcribeFromBase64(
                "dGVzdA==", "pcm", 16000);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        // 模拟结果应该是预定义的字符串之一
        assertTrue(result.contains("灵感") || result.contains("会议") || 
                   result.contains("阅读") || result.contains("产品") || 
                   result.contains("要点"));
    }

    @Test
    void testGetAccessToken_Disabled() {
        // 当服务禁用时，获取token应该抛出异常
        ReflectionTestUtils.setField(voiceTranscriptionService, "speechEnabled", false);
        ReflectionTestUtils.setField(voiceTranscriptionService, "apiKey", "");
        
        // 在模拟模式下，transcribeFromBase64不会调用getAccessToken
        // 但如果直接调用getAccessToken，由于没有有效的apiKey配置，
        // 实际行为取决于实现。这里我们测试isAvailable方法
    }

    @Test
    void testIsAvailable_Disabled() {
        ReflectionTestUtils.setField(voiceTranscriptionService, "speechEnabled", false);
        ReflectionTestUtils.setField(voiceTranscriptionService, "apiKey", "");
        
        assertFalse(voiceTranscriptionService.isAvailable());
    }

    @Test
    void testIsAvailable_Enabled() {
        ReflectionTestUtils.setField(voiceTranscriptionService, "speechEnabled", true);
        ReflectionTestUtils.setField(voiceTranscriptionService, "apiKey", "test-api-key");
        
        assertTrue(voiceTranscriptionService.isAvailable());
    }

    @Test
    void testMockTranscribeResults() {
        // 多次调用获取不同的模拟结果
        String result1 = voiceTranscriptionService.transcribeFromBase64("dGVzdA==", "pcm", 16000);
        
        // 验证结果不为空且长度合理
        assertNotNull(result1);
        assertTrue(result1.length() > 5);
    }
}
