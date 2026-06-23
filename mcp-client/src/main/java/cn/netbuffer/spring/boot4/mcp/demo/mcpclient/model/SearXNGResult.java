package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.model;

import lombok.Data;
import java.util.List;

/**
 * SearXNG搜索结果模型
 */
@Data
public class SearXNGResult {
    
    /**
     * 搜索查询字符串
     */
    private String query;
    
    /**
     * 原始搜索结果列表
     */
    private List<SearchResult> searchResults;
    
    /**
     * LLM增强后的回答
     */
    private String llmEnhancedAnswer;
    
    /**
     * 是否使用了LLM增强
     */
    private boolean isLlmEnhanced;
    
    /**
     * 搜索耗时（毫秒）
     */
    private long searchTime;
    
    /**
     * LLM处理耗时（毫秒）
     */
    private long llmProcessTime;
    
    /**
     * 搜索结果条目
     */
    @Data
    public static class SearchResult {
        private String title;
        private String url;
        private String content;
        private String engine;
        private String category;
    }
}