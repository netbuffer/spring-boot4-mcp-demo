package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.controller;

import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.component.SSEServerComponent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.Map;

/**
 * SSE 聊天控制器
 * <p>提供基于 SSE 的服务端推送聊天接口</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/chat/sse")
@CrossOrigin(origins = {"*"})
public class SSEChatController {

    @Resource
    private SSEServerComponent sseServerComponent;

    /**
     * 建立 SSE 事件流连接
     *
     * @param userId 用户标识
     * @return SseEmitter 事件流
     */
    @GetMapping(path = "events", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public SseEmitter events(@RequestParam("userId") String userId) {
        return sseServerComponent.events(userId);
    }

    /**
     * 向指定用户发送消息
     *
     * @param data 包含 userId 和 message 的请求体
     * @return 发送结果
     */
    @PostMapping(path = "send")
    public boolean send(@RequestBody Map<String, String> data) {
        return sseServerComponent.send(data.get("userId"), data.get("message"));
    }

}