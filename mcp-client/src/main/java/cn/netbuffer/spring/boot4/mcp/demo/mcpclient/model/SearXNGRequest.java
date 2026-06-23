package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.model;

import lombok.Data;

/**
 * SearXNG搜索请求模型
 */
@Data
public class SearXNGRequest {
    
    /**
     * 搜索查询字符串
     */
    private String query;
    
    /**
     * 搜索结果格式，默认为json
     */
    private String format = "json";
    
    /**
     * 是否需要LLM增强回答，默认为true
     */
    private boolean llmEnhanced = true;
    
    /**
     * LLM模型选择，可选值：openai, deepseek
     */
    private String llmModel;
}