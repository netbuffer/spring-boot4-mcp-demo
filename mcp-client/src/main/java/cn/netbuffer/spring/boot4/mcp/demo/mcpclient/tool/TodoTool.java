package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TodoTool {

    @Tool(name = "add_todo", description = "添加todo任务，添加待办任务，接收任务名称，任务详情")
    public boolean add(String title, String content) {
        log.debug("接收到任务[{}]-[{}]", title, content);
        //write to db
        log.debug("任务[{}]-[{}]已写入数据库", title, content);
        return true;
    }

}