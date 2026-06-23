package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE（Server-Sent Events）服务端组件
 * <p>管理 SSE 连接和消息推送，支持多用户并发连接</p>
 */
@Slf4j
@Component
public class SSEServerComponent {

    /** 用户 SSE 连接池，key 为用户 ID，value 为 SseEmitter 实例 */
    private static final Map<String, SseEmitter> sseClients = new ConcurrentHashMap<>();

    /**
     * 创建 SSE 连接
     *
     * @param userId 用户标识
     * @return SseEmitter 实例
     */
    public SseEmitter events(String userId) {
        SseEmitter sseEmitter = new SseEmitter(0L);
        Runnable cleanup = () -> {
            sseClients.remove(userId);
            log.info("SSE客户端已移除，用户ID为：{}", userId);
        };
        sseEmitter.onTimeout(() -> {
            log.warn("SSE连接超时，用户ID为：{}", userId);
            cleanup.run();
        });
        sseEmitter.onCompletion(() -> {
            log.info("SSE连接关闭，用户ID为：{}", userId);
            cleanup.run();
        });
        sseEmitter.onError((throwable) -> {
            log.error("SSE连接出错，用户ID为：" + userId, throwable);
            cleanup.run();
        });
        sseClients.put(userId, sseEmitter);
        log.info("SSE连接创建成功，连接的用户ID为：{}", userId);
        return sseEmitter;
    }

    /**
     * 向指定用户推送消息
     *
     * @param userId  用户标识
     * @param message 消息内容
     * @return 发送成功返回 true，否则返回 false
     */
    public boolean send(String userId, String message) {
        SseEmitter sseEmitter = sseClients.get(userId);
        if (sseEmitter == null) {
            log.warn("SSE连接不存在，用户ID为：{}", userId);
            return false;
        }
        try {
            sseEmitter.send(message);
            return true;
        } catch (IOException e) {
            log.error("发送消息失败，用户ID为：" + userId, e);
            return false;
        }
    }

}
