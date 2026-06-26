package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.RedisClient;

@Configuration
@ConditionalOnProperty(prefix = "spring.ai.chat.memory.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ChatClientConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Bean
    public RedisClient jedisClient() {
        if (!redisPassword.isEmpty()) {
            return RedisClient.builder()
                    .fromURI("redis://:" + redisPassword + "@" + redisHost + ":" + redisPort)
                    .build();
        }
        return RedisClient.builder().hostAndPort(redisHost, redisPort).build();
    }

}
