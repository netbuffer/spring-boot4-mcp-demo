package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.impl;

import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.constant.CDateTimeFormat;
import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.LLMChatClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RAGService {

    @jakarta.annotation.Resource
    private VectorStore vectorStore;
    @jakarta.annotation.Resource
    private StringRedisTemplate stringRedisTemplate;
    @jakarta.annotation.Resource(name = "deepseekChatClient")
    private LLMChatClient llmChatClient;

    /**
     * 上传并解析文档，分割后存入向量库（幂等覆盖：先删除同名文档再写入）
     *
     * @param resource 文档资源
     * @param fileName 文件名（作为唯一标识）
     * @return 分割后的文档列表
     */
    public List<Document> create(Resource resource, String fileName) {
        log.debug("create knowledge fileName={}", fileName);
        int removed = deleteByManifest(fileName);
        if (removed > 0) {
            log.debug("delete old documents size={}", removed);
        }
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
        List<Document> documentList = tikaDocumentReader.get();
        documentList.forEach(d -> {
            Map<String, Object> metaData = d.getMetadata();
            metaData.put("fileName", fileName);
            metaData.put("updateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern(CDateTimeFormat.yyyy_MM_dd_HH_mm_ss)));
        });
        log.debug("Original documentList.size()={}", documentList.size());

        TextSplitter textSplitter = TokenTextSplitter.builder().build();
        List<Document> splitDocuments = textSplitter.apply(documentList);
        splitDocuments.forEach(d -> d.getMetadata().put("fileName", fileName));
        splitDocuments = withDeterministicIds(splitDocuments, fileName);
        log.debug("Split documents.size()={}", splitDocuments.size());
        vectorStore.add(splitDocuments);
        saveManifest(fileName, collectIds(splitDocuments));
        return splitDocuments;
    }

    /**
     * 为文档分片生成基于文件名的稳定唯一 ID（格式：kb:{fileName}:{index}）
     * <p>实现幂等覆盖，同一文件重新上传后 ID 保持不变</p>
     */
    private List<Document> withDeterministicIds(List<Document> docs, String fileName) {
        String enc = urlEncode(fileName);
        List<Document> rebuilt = new ArrayList<>(docs.size());
        for (int i = 0; i < docs.size(); i++) {
            Document src = docs.get(i);
            String id = String.format("kb:%s:%03d", enc, i);
            HashMap<String, Object> meta = new HashMap<>(src.getMetadata());
            meta.put("fileName", fileName);
            Document dest = new Document(id, src.getText(), meta);
            rebuilt.add(dest);
        }
        return rebuilt;
    }

    private List<String> collectIds(List<Document> docs) {
        List<String> ids = new ArrayList<>();
        for (Document d : docs) {
            String id = d.getId();
            if (id != null) ids.add(id);
        }
        return ids;
    }

    private int deleteByManifest(String fileName) {
        List<String> oldIds = loadManifest(fileName);
        if (oldIds.isEmpty()) return 0;
        vectorStore.delete(oldIds);
        deleteManifest(fileName);
        return oldIds.size();
    }

    private List<String> loadManifest(String fileName) {
        String key = manifestKey(fileName);
        List<String> ids = stringRedisTemplate.opsForList().range(key, 0, -1);
        return ids == null ? new ArrayList<>() : ids;
    }

    private void saveManifest(String fileName, List<String> ids) {
        String key = manifestKey(fileName);
        if (ids == null || ids.isEmpty()) {
            deleteManifest(fileName);
            return;
        }
        stringRedisTemplate.delete(key);
        stringRedisTemplate.opsForList().rightPushAll(key, ids);
    }

    private void deleteManifest(String fileName) {
        stringRedisTemplate.delete(manifestKey(fileName));
    }

    private String manifestKey(String fileName) {
        return "kb:manifest:" + urlEncode(fileName);
    }

    private String urlEncode(String value) {
        String enc = URLEncoder.encode(value, StandardCharsets.UTF_8);
        return enc.replace("+", "%20");
    }

    /**
     * 在向量库中执行相似度搜索
     *
     * @param query 查询文本
     * @return 相似文档列表
     */
    public List<Document> search(String query) {
        return vectorStore.similaritySearch(query);
    }

    /**
     * 基于知识库内容检索并利用 LLM 回答问题（RAG 核心流程）
     *
     * @param question 用户问题
     * @return 根据检索到的知识生成的回答
     */
    public String askWithKnowledge(String question) {

        List<Document> relevantDocs = this.search(question);
        log.debug("Found {} relevant documents", relevantDocs.size());

        String context = relevantDocs.stream()
                .limit(5)
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        String prompt = buildRAGPrompt(question, context);
        log.trace("RAG prompt: {}", prompt);

        return llmChatClient.q(prompt);
    }

    private String buildRAGPrompt(String question, String context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个智能助手，请基于以下知识库内容回答用户问题。\n\n");
        
        if (!context.isEmpty()) {
            prompt.append("知识库内容：\n");
            prompt.append(context);
            prompt.append("\n\n");
        } else {
            prompt.append("知识库中没有找到相关内容。\n\n");
        }
        
        prompt.append("用户问题：");
        prompt.append(question);
        prompt.append("\n\n");
        
        prompt.append("请基于上述知识库内容回答用户问题。如果知识库中没有相关信息，请明确说明并提供一般性的建议。回答要准确、简洁、有帮助。");
        
        return prompt.toString();
    }

}
