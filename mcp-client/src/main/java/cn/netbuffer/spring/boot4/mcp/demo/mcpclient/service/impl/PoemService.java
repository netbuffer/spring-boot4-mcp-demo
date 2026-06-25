package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.impl;

import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.model.Poem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.template.ValidationMode;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PoemService {

    private final ChatClient poemChatClient;

    private final PromptTemplate userPrompt;

    public PoemService(DeepSeekChatModel deepSeekChatModel) {
        StTemplateRenderer renderer = StTemplateRenderer.builder()
                .validationMode(ValidationMode.WARN)
                .build();
        this.userPrompt = PromptTemplate.builder()
                .resource(new ClassPathResource("prompts/poem-recommend.st"))
                .renderer(renderer)
                .build();
        this.poemChatClient = ChatClient.builder(deepSeekChatModel).build();
    }

    public List<Poem> recommend(int count, String dynasty) {
        log.debug("recommend {} {} poems", count, dynasty);
        String rendered = userPrompt.render(Map.of("count", String.valueOf(count), "dynasty", dynasty));
        return poemChatClient.prompt()
                .user(u -> u.text(rendered))
                .call()
                .entity(new ParameterizedTypeReference<List<Poem>>() {});
    }

}
