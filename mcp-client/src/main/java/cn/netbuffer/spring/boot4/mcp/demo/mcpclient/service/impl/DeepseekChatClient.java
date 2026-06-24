package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.impl;

import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.component.RAGAdvisor;
import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.LLMChatClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.template.ValidationMode;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
public class DeepseekChatClient implements LLMChatClient {

    private ChatClient deepseekChatClient;
    private ChatMemory chatMemory;

    public DeepseekChatClient(DeepSeekChatModel deepSeekChatModel, ChatMemory chatMemory,
                              ToolCallbackProvider toolCallbackProvider, VectorStore vectorStore) {
        this.chatMemory = chatMemory;
        StTemplateRenderer renderer = StTemplateRenderer.builder()
                .validationMode(ValidationMode.WARN)
                .build();
        PromptTemplate systemPrompt = PromptTemplate.builder()
                .resource(new ClassPathResource("prompts/chat-system.st"))
                .renderer(renderer)
                .build();
        String renderedSystem = systemPrompt.render(Map.of(
                "currentDateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ));
        this.deepseekChatClient = ChatClient.builder(deepSeekChatModel)
                .defaultTools(toolCallbackProvider)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new RAGAdvisor(vectorStore),
                        new SimpleLoggerAdvisor()
                )
                .defaultSystem(renderedSystem)
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
