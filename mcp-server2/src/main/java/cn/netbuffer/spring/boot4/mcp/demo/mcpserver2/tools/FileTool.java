package cn.netbuffer.spring.boot4.mcp.demo.mcpserver2.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 文件操作 MCP Tool
 * <p>提供创建文件等工具方法，供 AI 模型调用</p>
 */
@Slf4j
@Component
public class FileTool {

    /**
     * 在用户本地磁盘创建文件并写入内容
     *
     * @param filePath 文件路径（绝对路径）
     * @param content  文件内容（UTF-8 编码）
     * @return 创建成功返回 true，否则返回 false
     */
    @Tool(name = "create_file", description = "创建文件内容到用户本地磁盘")
    public static boolean createFile(String filePath, String content) {
        log.debug("createFile invoked with path: {}", filePath);

        if (filePath == null || filePath.trim().isEmpty()) {
            log.error("文件路径不能为空");
            return false;
        }

        if (content == null) {
            log.warn("文件内容为null，忽略写入操作");
            return false;
        }

        try {
            Files.writeString(Path.of(filePath), content, StandardCharsets.UTF_8);
            log.info("成功创建文件: {}", filePath);
            return true;
        } catch (IOException e) {
            log.error("写入文件失败: {}", filePath, e);
            return false;
        }

    }

}