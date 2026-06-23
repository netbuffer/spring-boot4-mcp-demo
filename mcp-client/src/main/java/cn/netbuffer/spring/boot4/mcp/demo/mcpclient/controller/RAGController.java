package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.controller;

import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.impl.RAGService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * RAG（检索增强生成）控制器
 * <p>提供知识库文档上传、检索和基于知识库的问答接口</p>
 */
@Slf4j
@RestController
@RequestMapping("/rag")
public class RAGController {

    @Resource
    private RAGService ragService;

    /**
     * 上传知识文档，解析后存入向量库
     *
     * @param file 上传的文档文件（PDF、Word、TXT 等）
     * @return 分割后的文档列表
     */
    @PostMapping("knowledge/create")
    public List<Document> uploadKnowledge(@RequestParam("file") MultipartFile file) {
        return ragService.create(file.getResource(), file.getOriginalFilename());
    }

    /**
     * 在知识库中搜索相似文档
     *
     * @param q 搜索关键词
     * @return 相似文档列表
     */
    @GetMapping("knowledge/search")
    public List<Document> searchKnowledge(String q) {
        return ragService.search(q);
    }

    /**
     * 基于知识库内容回答问题
     *
     * @param question 用户问题
     * @return LLM 根据检索到的知识生成的回答
     */
    @GetMapping("ask")
    public String askWithKnowledge(String question) {
        log.debug("RAG ask question: {}", question);
        return ragService.askWithKnowledge(question);
    }

}
