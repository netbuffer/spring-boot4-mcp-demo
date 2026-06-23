package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.controller;

import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.model.ApiResponse;
import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.model.SearXNGRequest;
import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.model.SearXNGResult;
import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.impl.SearXNGService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * SearXNG搜索控制器，提供基于SearXNG和LLM的联网搜索API
 */
@Slf4j
@RestController
@RequestMapping("/SearXNG")
public class SearXNGController {

    @Resource
    private SearXNGService searXNGService;

    /**
     * GET 方式的搜索接口，支持查询参数
     *
     * @param q          搜索关键词
     * @param llmEnhanced 是否使用 LLM 增强回答
     * @param llmModel    LLM 模型类型（openai / deepseek）
     * @return 搜索结果响应
     */
    @GetMapping("search")
    public ApiResponse<SearXNGResult> search(@RequestParam String q,
                                             @RequestParam(required = false, defaultValue = "true") boolean llmEnhanced,
                                             @RequestParam(required = false) String llmModel) {
        log.info("Received search request with query: {}", q);
        SearXNGRequest request = new SearXNGRequest();
        request.setQuery(q);
        request.setLlmEnhanced(llmEnhanced);
        request.setLlmModel(llmModel);
        try {
            SearXNGResult result = searXNGService.search(request);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("Error in search endpoint: {}", e.getMessage());
            log.error("", e);
            return ApiResponse.fail(500, "搜索过程中发生错误: " + e.getMessage());
        }
    }

    /**
     * POST方式的搜索接口，支持更丰富的请求参数
     */
    /**
     * POST 方式的搜索接口，支持更丰富的请求参数
     *
     * @param request 搜索请求体（含 query、llmEnhanced、llmModel）
     * @return 搜索结果响应
     */
    @PostMapping("search")
    public ApiResponse<SearXNGResult> search(@RequestBody SearXNGRequest request) {
        log.info("Received POST search request with query: {}", request.getQuery());
        try {
            SearXNGResult result = searXNGService.search(request);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("Error in POST search endpoint: {}", e.getMessage(), e);
            return ApiResponse.fail(500, "搜索过程中发生错误: " + e.getMessage());
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("health")
    public ApiResponse<String> health() {
        return ApiResponse.success("SearXNG search service is healthy");
    }
}
