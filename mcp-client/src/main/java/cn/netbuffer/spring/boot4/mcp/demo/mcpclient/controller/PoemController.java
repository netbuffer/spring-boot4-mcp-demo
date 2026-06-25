package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.controller;

import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.constant.CDynasty;
import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.model.ApiResponse;
import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.model.Poem;
import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.impl.PoemService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat")
public class PoemController {

    @Resource
    private PoemService poemService;

    @GetMapping("/poems")
    public ApiResponse<List<Poem>> poems(@RequestParam(defaultValue = "3") int count,
                                         @RequestParam(defaultValue = "唐代") String dynasty) {
        if (!CDynasty.isValid(dynasty)) {
            return ApiResponse.fail(400, "不支持的朝代: " + dynasty);
        }
        log.debug("request {} {} poems", count, dynasty);
        return ApiResponse.success(poemService.recommend(count, dynasty));
    }

}
