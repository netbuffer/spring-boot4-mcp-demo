package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.config;

import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.chat.memory.RedisChatMemory;
import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.message.ChatMessage;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 聊天客户端配置类
 * <p>配置 Redis 序列化模板和聊天记忆组件</p>
 */
@Configuration
public class ChatClientConfig {

    /**
     * 构建支持多态序列化的 RedisTemplate，用于持久化聊天消息
     * <p>启用 Jackson 默认类型信息嵌入，确保 ChatMessage 多态类型正确序列化/反序列化</p>
     */
    @Bean("springAiRedisTemplate")
    @ConditionalOnProperty(prefix = "spring.ai.chat.memory.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RedisTemplate<String, ChatMessage> buildSpringAiRedisTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, ChatMessage> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        ObjectMapper objectMapper = JsonMapper.builder()
                .activateDefaultTyping(
                        BasicPolymorphicTypeValidator.builder().allowIfSubType(Object.class).build(),
                        DefaultTyping.NON_FINAL_AND_ENUMS,
                        JsonTypeInfo.As.PROPERTY
                )
                .build();
        GenericJacksonJsonRedisSerializer jsonSerializer = new GenericJacksonJsonRedisSerializer(objectMapper);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 注册基于 Redis 的聊天记忆组件
     */
    @Bean
    public ChatMemory redisChatMemory(RedisTemplate<String, ChatMessage> springAiRedisTemplate) {
        return new RedisChatMemory(springAiRedisTemplate);
    }

}