package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.core.Ordered;

import java.util.List;

@Slf4j
public class NullTextSanitizerAdvisor implements BaseAdvisor {

    private final int order;

    public NullTextSanitizerAdvisor() {
        this(Ordered.LOWEST_PRECEDENCE);
    }

    public NullTextSanitizerAdvisor(int order) {
        this.order = order;
    }

    @Override
    public String getName() {
        return "NullTextSanitizerAdvisor";
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        Prompt prompt = request.prompt();
        List<Message> instructions = prompt.getInstructions();
        boolean needsFix = instructions.stream()
                .anyMatch(m -> m instanceof AssistantMessage am && am.getText() == null);
        if (!needsFix) {
            return request;
        }
        log.debug("Sanitizing AssistantMessage with null text");
        List<Message> fixed = instructions.stream()
                .map(this::sanitize)
                .toList();
        Prompt fixedPrompt = new Prompt(fixed, prompt.getOptions());
        return request.mutate().prompt(fixedPrompt).build();
    }

    private Message sanitize(Message message) {
        if (message instanceof DeepSeekAssistantMessage dsam && dsam.getText() == null) {
            return new DeepSeekAssistantMessage.Builder()
                    .content("")
                    .reasoningContent(dsam.getReasoningContent())
                    .prefix(dsam.getPrefix())
                    .properties(dsam.getMetadata())
                    .toolCalls(dsam.getToolCalls())
                    .media(dsam.getMedia())
                    .build();
        }
        if (message instanceof AssistantMessage am && am.getText() == null) {
            return AssistantMessage.builder()
                    .content("")
                    .properties(am.getMetadata())
                    .toolCalls(am.getToolCalls())
                    .media(am.getMedia())
                    .build();
        }
        return message;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        return response;
    }
}
