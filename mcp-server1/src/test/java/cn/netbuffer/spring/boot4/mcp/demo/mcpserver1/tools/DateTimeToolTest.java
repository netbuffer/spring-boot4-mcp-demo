package cn.netbuffer.spring.boot4.mcp.demo.mcpserver1.tools;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DateTimeToolTest {

    @Test
    public void testIsWeekdayWithCurrentDate() {
        // 测试当前日期是否为工作日（周一至周五）
        // 由于当前日期可能变化，我们只验证方法能正确执行
        // 实际测试效果可以通过日志观察
        boolean result = DateTimeTool.isWeekday();
        System.out.println("Is today weekday: " + result);
        // 注意：这里不使用assertTrue/assertFalse，因为当前日期可能变化
        }

    @Test
    public void testIsWeekdayWithSpecificDates() {
        // 测试已知工作日（周一至周五）
        assertTrue(DateTimeTool.isWeekday(LocalDate.parse("2026-06-22")));  // 周一
        assertTrue(DateTimeTool.isWeekday(LocalDate.parse("2026-06-23")));  // 周二
        assertTrue(DateTimeTool.isWeekday(LocalDate.parse("2026-06-24")));  // 周三
        assertTrue(DateTimeTool.isWeekday(LocalDate.parse("2026-06-25")));  // 周四
        assertTrue(DateTimeTool.isWeekday(LocalDate.parse("2026-06-26")));  // 周五
        // 测试周末
        assertFalse(DateTimeTool.isWeekday(LocalDate.parse("2026-06-20")));  // 周六
        assertFalse(DateTimeTool.isWeekday(LocalDate.parse("2026-06-21")));  // 周日
    }

}