package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.impl;

import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.LLMChatClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * OpenAI 聊天客户端实现
 * <p>集成 OpenAI 大模型，支持 MCP Tool 调用</p>
 */
@Slf4j
@Component
public class OpenAIChatClient implements LLMChatClient {

    private ChatClient openAiChatClient;

    public OpenAIChatClient(OpenAiChatModel openAiChatModel,ToolCallbackProvider toolCallbackProvider) {
        this.openAiChatClient = ChatClient.builder(openAiChatModel)
                .defaultTools(toolCallbackProvider)
                .defaultSystem("你是一个全能的人工智能助手，可以回答任何问题。")
                .build();
        log.info("init OpenAIChatClient");
    }

    @Override
    public String q(String prompt) {
        return openAiChatClient.prompt(prompt).call().content();
    }

    @Override
    public Flux<String> streamq(String prompt) {
        return openAiChatClient.prompt(prompt).stream().content();
    }

}
