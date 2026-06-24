package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.impl;

import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.LLMChatClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.template.ValidationMode;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
public class OpenAIChatClient implements LLMChatClient {

    private ChatClient openAiChatClient;

    public OpenAIChatClient(OpenAiChatModel openAiChatModel, ToolCallbackProvider toolCallbackProvider) {
        StTemplateRenderer renderer = StTemplateRenderer.builder()
                .validationMode(ValidationMode.NONE)
                .build();
        PromptTemplate systemPrompt = PromptTemplate.builder()
                .resource(new ClassPathResource("prompts/chat-system.st"))
                .renderer(renderer)
                .build();
        String renderedSystem = systemPrompt.render(Map.of(
                "currentDateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ));
        this.openAiChatClient = ChatClient.builder(openAiChatModel)
                .defaultTools(toolCallbackProvider)
                .defaultSystem(renderedSystem)
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
