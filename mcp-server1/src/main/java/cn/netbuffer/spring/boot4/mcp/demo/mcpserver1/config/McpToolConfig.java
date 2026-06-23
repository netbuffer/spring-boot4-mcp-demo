package cn.netbuffer.spring.boot4.mcp.demo.mcpserver1.config;

import cn.netbuffer.spring.boot4.mcp.demo.mcpserver1.tools.DateTimeTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP Server1 工具配置类
 * <p>将 DateTimeTool 注册为 MCP Tool，供 AI 模型调用</p>
 */
@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider buildToolCallbackProvider(DateTimeTool dateTimeTool) {
        return MethodToolCallbackProvider.builder().toolObjects(dateTimeTool).build();
    }

}
