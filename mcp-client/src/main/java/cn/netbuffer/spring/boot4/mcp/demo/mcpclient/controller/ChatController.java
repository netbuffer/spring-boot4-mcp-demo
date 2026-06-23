package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.controller;

import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.LLMChatClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * LLM 聊天控制器
 * <p>提供与大模型的对话接口，支持同步和流式响应</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Resource(name = "deepseekChatClient")
    private LLMChatClient llmChatClient;

    /**
     * 发送问题并获取完整回答
     *
     * @param question 用户问题
     * @return 模型回答文本
     */
    @GetMapping("/q")
    public String q(@RequestParam String question) {
        log.debug("user asked question: {}", question);
        return llmChatClient.q(question);
    }

    /**
     * 发送问题并以流式方式获取回答
     *
     * @param question 用户问题
     * @return 模型回答文本流
     */
    @GetMapping("/streamq")
    public Flux<String> streamq(@RequestParam String question) {
        log.debug("user asked question: {}", question);
        return llmChatClient.streamq(question);
    }

}