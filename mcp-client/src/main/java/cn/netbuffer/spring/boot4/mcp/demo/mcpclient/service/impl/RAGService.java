package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.impl;

import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.constant.CDateTimeFormat;
import cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service.LLMChatClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
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

@Slf4j
@Service
public class RAGService {

    @jakarta.annotation.Resource
    private VectorStore vectorStore;
    @jakarta.annotation.Resource
    private StringRedisTemplate stringRedisTemplate;
    @jakarta.annotation.Resource(name = "deepseekChatClient")
    private LLMChatClient llmChatClient;

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

        List<Document> splitDocuments = TokenTextSplitter.builder().build().apply(documentList);
        splitDocuments.forEach(d -> d.getMetadata().put("fileName", fileName));
        splitDocuments = withDeterministicIds(splitDocuments, fileName);
        log.debug("Split documents.size()={}", splitDocuments.size());
        vectorStore.add(splitDocuments);
        saveManifest(fileName, collectIds(splitDocuments));
        return splitDocuments;
    }

    private List<Document> withDeterministicIds(List<Document> docs, String fileName) {
        String enc = urlEncode(fileName);
        List<Document> rebuilt = new ArrayList<>(docs.size());
        for (int i = 0; i < docs.size(); i++) {
            Document src = docs.get(i);
            String id = String.format("kb:%s:%03d", enc, i);
            HashMap<String, Object> meta = new HashMap<>(src.getMetadata());
            meta.put("fileName", fileName);
            rebuilt.add(new Document(id, src.getText(), meta));
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

    public List<Document> search(String query) {
        return vectorStore.similaritySearch(query);
    }

    public String askWithKnowledge(String question) {
        log.debug("RAG ask question: {}", question);
        return llmChatClient.q(question);
    }

}
