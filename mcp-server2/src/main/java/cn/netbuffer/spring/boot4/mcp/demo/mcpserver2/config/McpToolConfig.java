package cn.netbuffer.spring.boot4.mcp.demo.mcpserver2.config;

import cn.netbuffer.spring.boot4.mcp.demo.mcpserver2.tools.FileTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP Server2 工具配置类
 * <p>将 FileTool 注册为 MCP Tool，供 AI 模型调用</p>
 */
@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider buildToolCallbackProvider(FileTool dateTimeTool) {
        return MethodToolCallbackProvider.builder().toolObjects(dateTimeTool).build();
    }

}
