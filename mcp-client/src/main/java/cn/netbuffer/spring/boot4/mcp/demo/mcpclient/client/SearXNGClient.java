package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.client;

import com.alibaba.fastjson2.JSONObject;
import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.Query;

/**
 * SearXNG 搜索引擎 HTTP 客户端
 * <p>基于 Forest 框架声明式 HTTP 调用 SearXNG 搜索 API</p>
 */
@BaseRequest(
        baseURL = "{searxng.url}"
)
public interface SearXNGClient {

    /**
     * 执行搜索请求
     *
     * @param query  搜索关键词
     * @param format 返回格式（如 json）
     * @return 搜索结果 JSON
     */
    @Get("search")
    JSONObject search(@Query("q") String query, @Query("format") String format);

}
