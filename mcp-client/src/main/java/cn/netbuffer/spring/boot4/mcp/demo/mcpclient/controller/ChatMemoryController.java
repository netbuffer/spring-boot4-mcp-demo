package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.controller;

import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.model.ApiResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/chat/memory")
public class ChatMemoryController {

    @Resource
    private ChatMemory chatMemory;

    @DeleteMapping
    public ApiResponse<Void> clearMemory(
            @RequestParam(defaultValue = "default") String conversationId) {
        log.debug("clearing chat memory for conversation: {}", conversationId);
        chatMemory.clear(conversationId);
        return ApiResponse.success(null);
    }

}
