package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.message;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import java.io.Serializable;
import java.util.Map;

/**
 * 聊天消息模型
 * <p>用于在 Spring AI Message 和持久化/传输对象之间转换</p>
 */
@Data
@NoArgsConstructor
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息类型枚举 */
    public enum MessageType {USER, ASSISTANT, SYSTEM}

    /** 消息类型 */
    private MessageType messageType;
    /** 消息文本 */
    private String text;
    /** 元数据 */
    private Map<String, Object> metaData;

    /**
     * 根据 Spring AI Message 构建 ChatMessage
     *
     * @param message Spring AI 消息对象
     */
    public ChatMessage(Message message) {
        this.messageType = getMessageType(message);
        this.text = message.getText();
        this.metaData = message.getMetadata();
    }

    /**
     * 从 Spring AI Message 中提取消息类型
     *
     * @param message Spring AI 消息对象
     * @return 对应的 MessageType 枚举
     */
    private MessageType getMessageType(Message message) {
        if (message instanceof SystemMessage) return MessageType.SYSTEM;
        if (message instanceof UserMessage) return MessageType.USER;
        if (message instanceof AssistantMessage) return MessageType.ASSISTANT;
        throw new IllegalArgumentException("invalid message type: " + message.getClass().getName());
    }

    /**
     * 转换为 Spring AI Message 对象
     *
     * @return Spring AI 消息对象
     */
    public Message toMessage() {
        switch (messageType) {
            case SYSTEM:
                return new SystemMessage(text);
            case ASSISTANT:
                return new AssistantMessage(text);
            case USER:
                return new UserMessage(text);
            default:
                throw new IllegalArgumentException("invalid message type: " + messageType);
        }
    }

    /**
     * 全参构造
     *
     * @param messageType 消息类型
     * @param text        消息文本
     * @param metaData    元数据
     */
    public ChatMessage(MessageType messageType, String text, Map<String, Object> metaData) {
        this.messageType = messageType;
        this.text = text;
        this.metaData = metaData;
    }

}