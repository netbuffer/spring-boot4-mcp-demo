package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/embedding")
public class EmbeddingTestController {

    @Resource
    private EmbeddingModel embeddingModel;

    @GetMapping("/compare")
    public Map<String, Object> compareTexts(
            @RequestParam("text1") String text1,
            @RequestParam("text2") String text2) {

        log.debug("比较文本1: {} 和 文本2: {}", text1, text2);

        // 调用模型获取两个文本的向量表示
        float[] vector1 = embeddingModel.embed(text1);
        float[] vector2 = embeddingModel.embed(text2);

        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("向量维度不匹配！");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        double euclideanSum = 0.0;

        // 计算点积、模长、以及欧式距离的平方和
        for (int i = 0; i < vector1.length; i++) {
            double v1 = vector1[i];
            double v2 = vector2[i];

            dotProduct += v1 * v2;
            normA += v1 * v1;
            normB += v2 * v2;

            double diff = v1 - v2;
            euclideanSum += diff * diff;
        }

        // 余弦相似度 = 点积 / (模长A * 模长B)
        // 值越接近 1 表示越相似
        double cosineSimilarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        
        // 欧氏距离 = sqrt(平方差之和)
        // 值越接近 0 表示越相似
        double euclideanDistance = Math.sqrt(euclideanSum);

        Map<String, Object> result = new HashMap<>();
        result.put("text1", text1);
        result.put("text2", text2);
        result.put("dimensions", vector1.length);
        result.put("cosineSimilarity", cosineSimilarity);
        result.put("euclideanDistance", euclideanDistance);

        return result;
    }
}
