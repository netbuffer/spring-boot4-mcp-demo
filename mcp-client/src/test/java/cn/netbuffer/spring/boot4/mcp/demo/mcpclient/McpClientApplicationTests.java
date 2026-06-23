package cn.netbuffer.spring.boot4.mcp.demo.mcpclient;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.mockito.Mockito;

@SpringBootTest(classes = McpClientApplicationTests.MinimalContext.class,
    properties = {"spring.ai.mcp.client.enabled=false", "spring.ai.openai.api-key=test", "spring.ai.deepseek.api-key=test"})
@Import(McpClientApplicationTests.TestConfig.class)
class McpClientApplicationTests {

    @Test
    void contextLoads() {
    }

    @SpringBootApplication(excludeName = {
        "org.springframework.ai.mcp.client.common.autoconfigure.McpClientAutoConfiguration",
        "org.springframework.ai.model.tool.autoconfigure.ToolCallingAutoConfiguration",
        "org.springframework.ai.openai.autoconfigure.OpenAiChatAutoConfiguration",
        "org.springframework.ai.model.deepseek.autoconfigure.DeepSeekChatAutoConfiguration"
    })
    static class MinimalContext {
        public static void main(String[] args) {
            SpringApplication.run(MinimalContext.class, args);
        }
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        ChatMemory chatMemory() {
            return Mockito.mock(ChatMemory.class);
        }

        @Bean
        ToolCallbackProvider toolCallbackProvider() {
            var mock = Mockito.mock(ToolCallbackProvider.class);
            Mockito.when(mock.getToolCallbacks()).thenReturn(new ToolCallback[0]);
            return mock;
        }

        @Bean
        DeepSeekChatModel deepSeekChatModel() {
            return Mockito.mock(DeepSeekChatModel.class);
        }

        @Bean
        OpenAiChatModel openAiChatModel() {
            return Mockito.mock(OpenAiChatModel.class);
        }

        @Bean
        VectorStore vectorStore() {
            return Mockito.mock(VectorStore.class);
        }

        @Bean
        StringRedisTemplate stringRedisTemplate() {
            return Mockito.mock(StringRedisTemplate.class);
        }
    }
}
