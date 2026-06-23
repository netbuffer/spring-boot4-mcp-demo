package cn.netbuffer.spring.boot4.mcp.demo.mcpserver1.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具 MCP Tool
 * <p>提供获取当前时间、判断工作日等工具方法，供 AI 模型调用</p>
 */
@Slf4j
@Component
public class DateTimeTool {

    /**
     * 获取当前日期时间，格式为 yyyy-MM-dd HH:mm:ss
     */
    @Tool(name = "get_current_datetime", description = "查询当前的日期和时间，返回格式为'yyyy-MM-dd HH:mm:ss'")
    public static String getCurrentDateTime() {
        log.debug("getCurrentDateTime invoked");
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 判断当前日期是否为周一到周五（工作日）
     *
     * @return 如果是周一到周五返回 true，否则返回 false
     */
    @Tool(name = "is_weekday", description = "查询当前是否为工作日，是否需要上班儿，返回true表示当前日期为工作日，否则返回false")
    public static boolean isWeekday() {
        log.debug("isWeekday invoked");
        LocalDate now = LocalDate.now();
        return isWeekday(now);
    }

    /**
     * 判断指定日期是否为周一到周五（工作日）
     *
     * @param date 要判断的日期
     * @return 如果是周一到周五返回 true，否则返回 false
     */
    public static boolean isWeekday(LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        return dayOfWeek >= 1 && dayOfWeek <= 5;
    }

}