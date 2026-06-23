package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.service;

import reactor.core.publisher.Flux;

/**
 * LLM 聊天客户端接口
 * <p>定义与大模型交互的统一方法，支持同步和流式响应</p>
 */
public interface LLMChatClient {

    /**
     * 发送提示词并获取完整响应
     *
     * @param prompt 提示词内容
     * @return 模型返回的文本
     */
    String q(String prompt);

    /**
     * 发送提示词并以流式方式获取响应
     *
     * @param prompt 提示词内容
     * @return 文本流的 Flux 响应
     */
    Flux<String> streamq(String prompt);

}
