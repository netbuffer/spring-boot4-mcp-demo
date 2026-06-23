# spring-boot4-mcp-demo

![jdk 21](https://img.shields.io/static/v1?label=jdk&message=21&color=blue)
![spring-boot](https://img.shields.io/static/v1?label=spring-boot&message=4.0.7&color=green)
![spring-ai](https://img.shields.io/static/v1?label=spring-ai&message=2.0.0&color=green)
![dotenv-java](https://img.shields.io/static/v1?label=dotenv-java&message=3.2.0&color=blue)
![postman](https://img.shields.io/static/v1?label=postman&message=v11.70.5&color=FF6C37)
![forest](https://img.shields.io/static/v1?label=forest&message=1.8.1&color=54C024)
![searxng:2026.6.20-fd42d4fda](https://img.shields.io/static/v1?label=searxng&message=2026.6.20-fd42d4fda&color=487CFF)
![node](https://img.shields.io/static/v1?label=node&message=24.9.0&color=blue)
![uvx](https://img.shields.io/static/v1?label=uvx&message=0.9.11&color=black)

https://github.com/netbuffer/spring-boot4-mcp-demo  
https://gitee.com/netbuffer/spring-boot4-mcp-demo

## help
* http://127.0.0.1:8081 - 主应用界面
* http://127.0.0.1:8380 - Redis UI (密码: your-pwd)
* http://127.0.0.1:8381/search?q=java%E6%9C%80%E6%96%B0%E7%89%88%E6%9C%AC%E6%98%AF%E5%A4%9A%E5%B0%91&format=json - Searxng示例
* https://modelcontextprotocol.io/docs/develop/connect-local-servers#using-the-filesystem-server - MCP文档
* https://lbsyun.baidu.com/faq/api?title=mcpserver/base - Baidu Map MCP文档
* https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html
* https://github.com/spring-projects/spring-ai-examples/blob/main/model-context-protocol/weather/starter-webmvc-server/src/main/java/org/springframework/ai/mcp/sample/server/McpServerApplication.java
* https://docs.spring.io/spring-ai/reference/api/chat-memory.html

### Project Structure

```
spring-boot4-mcp-demo/
├── mcp-client/          # 主应用模块（Web 控制器、服务、配置）
│   └── src/main/resources/application.yaml
├── mcp-common/          # 公共组件
├── mcp-server1/         # MCP服务器实现（SSE支持）
│   └── src/main/resources/application.yaml
├── mcp-server2/         # MCP服务器实现（标准IO支持）
│   └── src/main/resources/application.yaml
├── help/
│   ├── http-requests.http               # IDEA HTTP Client 测试集合
│   ├── spring-boot4-mcp-demo.postman_collection.json  # Postman测试集合
│   ├── rag-knowledge.md                 # RAG知识库说明
│   └── *.png                            # 项目截图资源
├── docker-compose.yaml  # Redis Stack（向量存储）
└── pom.xml
```

### Prerequisites

- JDK 21+
- Maven 3.9+
- Docker（用于 Redis Stack）
- Node.js (用于部分MCP服务器)
- Python 3.8+ (用于uvx工具)
- 环境变量（可选）：
  - `OPENAI_API_KEY`、`DEEPSEEK_API_KEY`

### Build & Run

```bash
# 构建
mvn clean install

# 启动 Redis Stack（向量存储）
docker-compose up -d
# Redis UI: http://localhost:8380  密码: your-pwd

# 运行应用
mvn spring-boot:run -pl mcp-client

# 可选 JVM 参数（Windows 控制台中文友好）
-Dfile.encoding=UTF-8
```

### Configuration

主配置文件：
- `mcp-client/src/main/resources/application.yaml` - 主应用配置
- `mcp-server1/src/main/resources/application.yaml` - MCP服务器配置
- `mcp-servers.json` - MCP服务配置文件，定义可用的MCP服务器

关键项：
- Server: port 8081，强制 UTF-8
- Spring AI Chat: OpenAI / DeepSeek（max-tokens、temperature、model）
- VectorStore Redis: `index-name=sb4md-v`, `prefix=vtr:`
- Embedding cache: `.env/transformer-cache`
- Redis: host 127.0.0.1, port 8379, password `your-pwd`

### API Endpoints

- **Chat**
  - GET `/api/chat/q?question=...` → String
  - GET `/api/chat/streamq?question=...` → Server streaming text (Flux)
- **SSE**
  - GET `/api/chat/sse/events?userId=xxx` → SseEmitter
  - POST `/api/chat/sse/send?userId=xxx` body `{ "userId": "...", "message": "..." }`
- **RAG 知识库**
  - POST `/rag/knowledge/create`（multipart/form-data）- 上传文档到知识库
    - 字段 `file`：文档（推荐 .docx）
  - GET `/rag/knowledge/search?q=...` → List<Document> - 检索相关文档
  - GET `/rag/ask?question=...` → String - 基于知识库的智能问答

测试示例见 `help/http-requests.http`，可直接在 IDEA 中运行。

### RAG 行为说明（概要）

- **文档解析**：Apache Tika 解析 .docx，全链路 UTF-8
- **文档分割**：使用 TokenTextSplitter 进行智能分片，避免 token 数量超限
- **向量化存储**：文档分片向量化后存储到 Redis VectorStore
- **重复上传幂等覆盖**：基于文件名的稳定向量 ID + Redis manifest，同名文件仅保留最新一版向量
- **智能检索**：基于语义相似度检索相关文档片段
- **RAG 问答**：结合检索到的知识库内容和大模型生成准确回答
- **中文文件名上传**：建议使用 RFC5987 `filename*`（见 http-requests.http 示例）

### 文件上传配置

- **单文件大小限制**：10MB
- **总请求大小限制**：11MB
- **支持格式**：.docx, .pdf, .txt 等Apache Tika支持的格式

### FAQ

- **启动报错：VectorStore bean not found**
  - 确认 Redis 可用（8379 端口），`spring.data.redis.*` 配置正确
  - 确认存在可用的 Embedding 能力（如 transformer/openai；必要时开启 `spring.ai.embedding.transformer.enabled=true`）
  - Maven Reimport + `mvn clean package`

- **中文文件名/内容乱码**
  - JVM 参数：`-Dfile.encoding=UTF-8`
  - 日志配置：`logging.charset.console/file=UTF-8`
  - 上传请求使用 `filename*`（见 http-requests.http 示例）

- **413 Request Entity Too Large**
  - 检查文件大小是否超过 10MB 限制
  - 可在 `application.yaml` 中调整 `spring.servlet.multipart.max-file-size` 和 `max-request-size`

- **RAG 问答返回空结果**
  - 确认知识库中已上传相关文档
  - 检查问题是否与文档内容相关
  - 调整检索参数或增加相关文档

- **大模型调用失败**
  - 检查 `OPENAI_API_KEY` 或 `DEEPSEEK_API_KEY` 环境变量是否设置
  - 确认网络连接正常（必要时配置代理）
  - 检查 API Key 是否有效且有足够配额

### proxy config
```shell
-Dhttp.proxyHost=xxx
-Dhttp.proxyPort=xxx
-Dhttps.proxyHost=xxx
-Dhttps.proxyPort=xxx
```

### Project Modules

This project contains the following modules:

1. **mcp-client** - Main application module with Spring Boot web controllers and services
2. **mcp-common** - Shared utilities and common components
3. **mcp-server1** - MCP服务器实现，提供SSE支持
   - 支持SSE流式协议
   - 监听端口8082
   - 可配置的MCP服务端点
   - 与主应用通过MCP协议通信
4. **mcp-server2** - MCP服务器实现，提供标准IO支持
   - 非Web应用类型，使用stdio模式
   - 异步处理模式
   - 与主应用通过MCP协议通信
### Features

- **RESTful API endpoints** - 完整的 REST API 支持
- **RAG 知识库系统** - 文档上传、向量化、智能检索和问答
- **多模型支持** - 集成 OpenAI 和 DeepSeek 大模型
- **向量存储** - 基于 Redis 的向量数据库支持
- **实时流式响应** - 支持流式对话和 SSE 推送
- **文档解析** - Apache Tika 支持多种文档格式
- **环境变量管理** - 使用 dotenv-java 管理配置
- **模块化架构** - 清晰的分层架构设计
- **中文支持** - 全链路 UTF-8 编码支持

### Reference Documentation

For further reference, please consider the following sections:

* https://github.com/searxng/searxng
* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.0.7/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.0.7/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.0.7/reference/web/servlet.html)
* https://github.com/leehainuo/spring-ai-redis-starter

### Guides

The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the
parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.


