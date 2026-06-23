package cn.netbuffer.spring.boot4.mcp.demo.mcpserver2;

import cn.netbuffer.spring.boot4.mcp.demo.mcpcommon.utils.DotEnvUtils;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MCP Server2 启动入口
 * <p>提供文件操作相关的 MCP Tool 服务</p>
 */
@SpringBootApplication
public class McpServer2Application {

    public static Dotenv DOTENV;

    /**
     * 启动前加载 mcp-server2.env 环境变量，然后启动 Spring Boot 应用
     */
    public static void main(String[] args) {
        DOTENV = DotEnvUtils.initDotEnv2SystemProperty("mcp-server2.env");
        SpringApplication.run(McpServer2Application.class, args);
    }

}
