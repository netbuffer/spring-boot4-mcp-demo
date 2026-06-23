package cn.netbuffer.spring.boot4.mcp.demo.mcpclient;

import cn.netbuffer.spring.boot4.mcp.demo.mcpcommon.utils.DotEnvUtils;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MCP Client 启动入口
 * <p>集成 MCP Server 的 Tool 调用、LLM 对话、SearXNG 联网搜索、RAG 知识库等功能</p>
 */
@SpringBootApplication
public class McpClientApplication {

    public static Dotenv DOTENV;

    /**
     * 启动前加载 mcp-client.env 环境变量，然后启动 Spring Boot 应用
     */
    public static void main(String[] args) {
        DOTENV = DotEnvUtils.initDotEnv2SystemProperty("mcp-client.env");
        SpringApplication.run(McpClientApplication.class, args);
    }

}
