package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.impl;

import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.client.SearXNGClient;
import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.model.SearXNGRequest;
import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.model.SearXNGResult;
import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.LLMChatClient;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SearXNG搜索服务实现，集成SearXNG API和LLM功能
 */
@Slf4j
@Service
public class SearXNGService {

    @Resource
    private SearXNGClient searXNGClient;
    
    @Resource(name = "openAIChatClient")
    private LLMChatClient openAIChatClient;
    
    @Resource(name = "deepseekChatClient")
    private LLMChatClient deepseekChatClient;

    /**
     * 执行 SearXNG 搜索，并根据参数决定是否使用 LLM 增强回答
     *
     * @param request 搜索请求参数（含关键词、LLM 增强开关、模型选择）
     * @return 搜索结果（含原始搜索结果和可选的 LLM 增强回答）
     */
    public SearXNGResult search(SearXNGRequest request) {
        SearXNGResult result = new SearXNGResult();
        result.setQuery(request.getQuery());
        
        try {
            long searchStart = System.currentTimeMillis();
            JSONObject searchResponse = searXNGClient.search(request.getQuery(), request.getFormat());
            long searchEnd = System.currentTimeMillis();
            result.setSearchTime(searchEnd - searchStart);
            
            List<SearXNGResult.SearchResult> searchResults = parseSearchResults(searchResponse);
            result.setSearchResults(searchResults);
            
            if (request.isLlmEnhanced() && !searchResults.isEmpty()) {
                long llmStart = System.currentTimeMillis();
                String enhancedAnswer = enhanceWithLLM(request.getQuery(), searchResults, request.getLlmModel());
                long llmEnd = System.currentTimeMillis();
                result.setLlmEnhancedAnswer(enhancedAnswer);
                result.setLlmProcessTime(llmEnd - llmStart);
                result.setLlmEnhanced(true);
            }
            
            log.info("Search completed for query: {}, results: {}, llmEnhanced: {}", 
                     request.getQuery(), searchResults.size(), result.isLlmEnhanced());
            
        } catch (Exception e) {
            log.error("Error during search: {}", e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * 解析 SearXNG 返回的 JSON 结果，提取标题、链接、内容等信息
     *
     * @param response SearXNG 返回的 JSON 响应
     * @return 解析后的搜索结果列表
     */
    private List<SearXNGResult.SearchResult> parseSearchResults(JSONObject response) {
        List<SearXNGResult.SearchResult> results = new ArrayList<>();
        
        try {
            if (response != null && response.containsKey("results")) {
                JSONArray resultArray = response.getJSONArray("results");
                for (int i = 0; i < resultArray.size(); i++) {
                    JSONObject item = resultArray.getJSONObject(i);
                    SearXNGResult.SearchResult searchResult = new SearXNGResult.SearchResult();
                    searchResult.setTitle(item.getString("title"));
                    searchResult.setUrl(item.getString("url"));
                    searchResult.setContent(item.getString("content"));
                    searchResult.setEngine(item.getString("engine"));
                    searchResult.setCategory(item.getString("category"));
                    results.add(searchResult);
                }
            }
        } catch (Exception e) {
            log.error("Error parsing search results: {}", e.getMessage(), e);
        }
        
        return results;
    }
    
    /**
     * 使用 LLM 对搜索结果进行增强总结
     *
     * @param query         用户原始问题
     * @param searchResults SearXNG 搜索结果列表
     * @param llmModel      指定的 LLM 模型（openai / deepseek），为空则使用默认
     * @return LLM 增强后的回答文本
     */
    private String enhanceWithLLM(String query, List<SearXNGResult.SearchResult> searchResults, String llmModel) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("请基于以下搜索结果，为用户的问题提供一个全面、准确的回答。\n\n");
        promptBuilder.append("用户问题：").append(query).append("\n\n");
        promptBuilder.append("搜索结果：\n");
        
        int resultCount = Math.min(5, searchResults.size());
        for (int i = 0; i < resultCount; i++) {
            SearXNGResult.SearchResult result = searchResults.get(i);
            promptBuilder.append("[结果").append(i + 1).append("]\n");
            promptBuilder.append("标题：").append(result.getTitle()).append("\n");
            promptBuilder.append("内容：").append(result.getContent()).append("\n");
            promptBuilder.append("来源：").append(result.getUrl()).append("\n\n");
        }
        
        promptBuilder.append("请基于以上搜索结果，简洁明了的回答用户问题，注意不要增加主观推测。");
        
        String prompt = promptBuilder.toString();
        log.debug("LLM enhancement prompt: {}", prompt);
        
        LLMChatClient llmChatClient = selectLLMClient(llmModel);
        
        try {
            return llmChatClient.q(prompt);
        } catch (Exception e) {
            log.error("Error enhancing with LLM: {}", e.getMessage(), e);
            return "无法使用LLM增强搜索结果，请稍后重试。";
        }
    }
    
    /**
     * 根据模型名称选择合适的LLM客户端
     */
    private LLMChatClient selectLLMClient(String llmModel) {
        return Optional.ofNullable(llmModel)
                .map(model -> {
                    if (model.equalsIgnoreCase("deepseek")) {
                        return deepseekChatClient;
                    }
                    return openAIChatClient;
                })
                .orElse(openAIChatClient); // 默认使用OpenAI
    }
}
