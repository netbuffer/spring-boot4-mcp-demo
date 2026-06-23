package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.impl;

import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.client.SearXNGClient;
import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.model.SearXNGRequest;
import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.model.SearXNGResult;
import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.LLMChatClient;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearXNGServiceTest {

    @Mock
    private SearXNGClient searXNGClient;

    @Mock
    @Qualifier("openaiChatClient")
    private LLMChatClient openaiChatClient;

    @Mock
    @Qualifier("deepseekChatClient")
    private LLMChatClient deepseekChatClient;

    @InjectMocks
    private SearXNGService searXNGService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSearchWithLlmEnhancement() {
        // 准备测试数据
        SearXNGRequest request = new SearXNGRequest();
        request.setQuery("Spring Boot");
        request.setLlmEnhanced(true);
        
        // 模拟SearXNG响应
        JSONObject mockResponse = new JSONObject();
        JSONArray resultsArray = new JSONArray();
        JSONObject result1 = new JSONObject();
        result1.put("title", "Spring Boot 官方文档");
        result1.put("url", "https://spring.io/projects/spring-boot");
        result1.put("content", "Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications that you can 'just run'.");
        result1.put("engine", "bing");
        result1.put("category", "general");
        resultsArray.add(result1);
        mockResponse.put("results", resultsArray);
        
        // 模拟LLM响应
        String mockLlmResponse = "Spring Boot是一个基于Spring框架的快速开发应用程序的工具，它可以帮助开发者快速创建独立的、生产级别的Spring应用程序。";
        
        // 设置mock行为
        when(searXNGClient.search("Spring Boot", "json")).thenReturn(mockResponse);
        when(openaiChatClient.q(anyString())).thenReturn(mockLlmResponse);
        
        // 执行测试
        SearXNGResult result = searXNGService.search(request);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("Spring Boot", result.getQuery());
        assertEquals(1, result.getSearchResults().size());
        assertEquals("Spring Boot 官方文档", result.getSearchResults().get(0).getTitle());
        assertTrue(result.isLlmEnhanced());
        assertEquals(mockLlmResponse, result.getLlmEnhancedAnswer());
        
        // 验证mock调用
        verify(searXNGClient, times(1)).search("Spring Boot", "json");
        verify(openaiChatClient, times(1)).q(anyString());
    }

    @Test
    void testSearchWithoutLlmEnhancement() {
        // 准备测试数据
        SearXNGRequest request = new SearXNGRequest();
        request.setQuery("Spring Boot");
        request.setLlmEnhanced(false);
        
        // 模拟SearXNG响应
        JSONObject mockResponse = new JSONObject();
        JSONArray resultsArray = new JSONArray();
        mockResponse.put("results", resultsArray);
        
        // 设置mock行为
        when(searXNGClient.search("Spring Boot", "json")).thenReturn(mockResponse);
        
        // 执行测试
        SearXNGResult result = searXNGService.search(request);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("Spring Boot", result.getQuery());
        assertFalse(result.isLlmEnhanced());
        assertNull(result.getLlmEnhancedAnswer());
        
        // 验证mock调用
        verify(searXNGClient, times(1)).search("Spring Boot", "json");
        verify(openaiChatClient, never()).q(anyString());
    }

    @Test
    void testSearchWithDeepseekModel() {
        // 准备测试数据
        SearXNGRequest request = new SearXNGRequest();
        request.setQuery("Spring Boot");
        request.setLlmEnhanced(true);
        request.setLlmModel("deepseek");
        
        // 模拟SearXNG响应
        JSONObject mockResponse = new JSONObject();
        JSONArray resultsArray = new JSONArray();
        JSONObject result1 = new JSONObject();
        result1.put("title", "Test Title");
        result1.put("url", "https://test.com");
        result1.put("content", "Test content");
        result1.put("engine", "bing");
        result1.put("category", "general");
        resultsArray.add(result1);
        mockResponse.put("results", resultsArray);
        
        // 模拟Deepseek响应
        String mockLlmResponse = "Deepseek enhanced answer";
        
        // 设置mock行为
        when(searXNGClient.search("Spring Boot", "json")).thenReturn(mockResponse);
        when(deepseekChatClient.q(anyString())).thenReturn(mockLlmResponse);
        
        // 执行测试
        SearXNGResult result = searXNGService.search(request);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(mockLlmResponse, result.getLlmEnhancedAnswer());
        
        // 验证mock调用
        verify(deepseekChatClient, times(1)).q(anyString());
        verify(openaiChatClient, never()).q(anyString());
    }

    @Test
    void testSearchWithEmptyResults() {
        // 准备测试数据
        SearXNGRequest request = new SearXNGRequest();
        request.setQuery("NonExistentQuery123456");
        request.setLlmEnhanced(true);
        
        // 模拟空结果响应
        JSONObject mockResponse = new JSONObject();
        mockResponse.put("results", new JSONArray());
        
        // 设置mock行为
        when(searXNGClient.search("NonExistentQuery123456", "json")).thenReturn(mockResponse);
        
        // 执行测试
        SearXNGResult result = searXNGService.search(request);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.getSearchResults().isEmpty());
        assertFalse(result.isLlmEnhanced());
        
        // 验证没有调用LLM
        verify(openaiChatClient, never()).q(anyString());
    }
}