package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.impl;

import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.LLMChatClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Deepseek 聊天客户端实现
 * <p>集成 Deepseek 大模型，支持 MCP Tool 调用和聊天记忆</p>
 */
@Slf4j
@Component
public class DeepseekChatClient implements LLMChatClient {

    private ChatClient deepseekChatClient;
    private ChatMemory chatMemory;

    public DeepseekChatClient(DeepSeekChatModel deepSeekChatModel, ChatMemory chatMemory, ToolCallbackProvider toolCallbackProvider) {
        this.chatMemory = chatMemory;
        this.deepseekChatClient = ChatClient.builder(deepSeekChatModel)
                .defaultTools(toolCallbackProvider)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(), new SimpleLoggerAdvisor())
                .defaultSystem("你是一个全能的人工智能助手，可以回答任何问题。")
                .build();
        log.info("init DeepseekChatClient");
    }

    @Override
    public String q(String prompt) {
        return deepseekChatClient.prompt(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "default"))
                .call().content();
    }

    @Override
    public Flux<String> streamq(String prompt) {
        Flux<String> content = deepseekChatClient.prompt(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "default"))
                .stream().content();
        return content.doOnNext(s -> log.info("stream output...:{}", s));
    }

}
