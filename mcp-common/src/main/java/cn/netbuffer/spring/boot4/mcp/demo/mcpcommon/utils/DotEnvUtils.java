package cn.netbuffer.spring.boot4.mcp.demo.mcpcommon.utils;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * .env 文件工具类，用于加载环境变量配置并注册到系统属性中
 */
public class DotEnvUtils {

    /**
     * 加载指定名称的 .env 文件，并将其所有键值对注册为系统属性
     *
     * @param filename .env 文件名（如 "mcp-server1.env"）
     * @return 加载后的 Dotenv 实例
     */
    public static Dotenv initDotEnv2SystemProperty(String filename) {
        Dotenv dotenv = Dotenv.configure()
                .filename(filename)
                .ignoreIfMissing()
                .load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        return dotenv;
    }

}
