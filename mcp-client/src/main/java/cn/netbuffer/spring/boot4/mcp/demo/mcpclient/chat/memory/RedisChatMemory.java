package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.chat.memory;

import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.message.ChatMessage;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.List;

/**
 * 基于 Redis List 的聊天记忆实现
 * <p>使用 Redis List 按会话 ID 存储聊天消息，支持添加、获取和清除历史记录</p>
 */
public class RedisChatMemory implements ChatMemory {

    private final RedisTemplate<String, ChatMessage> redisTemplate;
    private final ListOperations<String, ChatMessage> listOperations;

    public RedisChatMemory(RedisTemplate<String, ChatMessage> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.listOperations = redisTemplate.opsForList();
    }

    /**
     * 添加单条消息到会话历史
     */
    @Override
    public void add(String conversationId, Message message) {
        ChatMessage chatMessage = new ChatMessage(message);
        listOperations.leftPush(conversationId, chatMessage);
    }

    /**
     * 批量添加消息到会话历史
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        List<ChatMessage> chatMessages = messages.stream()
                .map(ChatMessage::new)
                .toList();
        if (CollectionUtils.isNotEmpty(chatMessages)) {
            listOperations.leftPushAll(conversationId, chatMessages);
        }
    }

    /**
     * 获取会话的所有历史消息
     */
    @Override
    public List<Message> get(String conversationId) {
        List<ChatMessage> chatMessages = listOperations.range(conversationId, 0, -1);
        if (CollectionUtils.isEmpty(chatMessages)) {
            return List.of();
        }
        return chatMessages.stream()
                .map(ChatMessage::toMessage)
                .toList();
    }

    /**
     * 清除指定会话的历史记录
     */
    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(conversationId);
    }

}