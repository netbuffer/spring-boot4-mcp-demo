# Agents 开发规范

> 适用于 Cursor、Codex、Claude Code、Windsurf 等所有 AI 编码助手的通用规约。

---

## 一、项目概述

Spring Boot 4 + Spring AI 2 + MCP（Model Context Protocol）演示项目。

| 模块 | 类型 | 端口 | 职责 |
|------|------|------|------|
| `mcp-common` | JAR 库 | — | 共享工具类（`DotEnvUtils`） |
| `mcp-client` | Spring Boot Web | 8081 | 主应用：Web 控制器、LLM 集成、MCP 客户端聚合、RAG、SearXNG |
| `mcp-server1` | Spring Boot Web | 8082 | 基于 SSE 的 MCP 服务器（DateTimeTool） |
| `mcp-server2` | Spring Boot 非 Web | — | 基于 Stdio 的 MCP 服务器（FileTool，`web-application-type: none`） |

---

## 二、技术栈

| 技术 | 版本 |
|------|------|
| Java | 25 |
| Spring Boot | 4.1.0 |
| Spring AI | 2.0.0 |
| MCP (Model Context Protocol) | Spring AI MCP Client / Server |
| Redis Stack | 7.4.0 (向量存储 + 聊天记忆) |
| SearXNG | 自托管元搜索引擎 |
| Forest | 1.8.1 (声明式 HTTP 客户端) |
| Fastjson2 | 2.0.62 |
| Lombok | 编译时注解 |
| Maven | 多模块构建 |

---

## 三、模块架构与数据流

### 3.1 MCP 连接三种传输方式

```
mcp-client (8081)
  ├── SSE ──────────> mcp-server1 (8082, DateTimeTool)
  ├── Stdio ─────────> mcp-server2 (FileTool, java -jar)
  └── Streamable HTTP ──> 百度地图 MCP (https://mcp.map.baidu.com)
```

### 3.2 聊天请求数据流

```
用户 → ChatController (/api/chat/q)
  → DeepseekChatClient
    → MessageChatMemoryAdvisor.before()  // 加载 Redis 历史
    → LLM 调用 (DeepSeek)
    → MCP Tool 回调（DateTimeTool / FileTool / 百度地图）
    → MessageChatMemoryAdvisor.after()   // 保存到 Redis
  → 返回文本
```

### 3.3 RAG 知识库流程

```
上传文档 → POST /rag/knowledge/create
  → TikaDocumentReader 解析
  → TokenTextSplitter 分割
  → 确定性 ID (kb:{文件名urlencode}:{index3})
  → vectorStore.add() → Redis 向量存储

提问 → GET /rag/ask?question=...
  → vectorStore.similaritySearch()
  → 构造含上下文提示词 → LLM 回答
```

### 3.4 外部服务依赖

- **Redis Stack**: 端口 8379 (数据) / 8380 (UI), 密码 `your-pwd`
- **SearXNG**: 端口 8381
- **OpenAI API** / **DeepSeek API**: 环境变量注入
- **百度地图 MCP API**: 环境变量注入（Streamable HTTP，默认注释，启用需取消 `application.yaml` 注释并配置 `BAIDU_MAP_API_KEY`）

---

## 四、包命名规范

```
cn.netbuffer.spring.boot4.mcp.demo.${module}.*

${module}:
  mcpclient   → mcp-client 模块
  mcpserver1  → mcp-server1 模块
  mcpserver2  → mcp-server2 模块
  mcpcommon   → mcp-common 模块

子包:
  .controller    — REST 控制器
  .service       — 业务接口
  .service.impl  — 业务实现
  .model         — 请求/响应 DTO
  .config        — @Configuration Bean 定义
  .component     — 共享组件
  .client        — 声明式 HTTP 客户端（Forest）
  .constant      — 常量类
  .tools         — MCP @Tool 定义
```

---

## 五、类命名规范

| 类别 | 命名模式 | 示例 |
|------|----------|------|
| 控制器 | `XxxController` | `ChatController` |
| 服务接口 | `XxxService` / `XxxChatClient` | `LLMChatClient` |
| 服务实现 | `XxxServiceImpl` / `XxxChatClient` | `DeepseekChatClient` |
| 请求 DTO | `XxxRequest` | `SearXNGRequest` |
| 响应 DTO | `XxxResult` / `XxxResponse` | `ApiResponse`, `SearXNGResult` |
| 常量类 | `CXxx` | `CDateTimeFormat` |
| 组件 | `XxxComponent` | `SSEServerComponent` |
| MCP 工具 | `XxxTool` | `DateTimeTool`, `FileTool` |
| 启动类 | `McpXxxApplication` | `McpClientApplication` |

---

## 六、代码规范

### 6.1 魔法字符串

调用 `Advisor` 参数时**禁止**直接写魔法字符串，必须使用 Spring AI 提供的常量：

```java
// ✅ 正确
import org.springframework.ai.chat.memory.ChatMemory;
.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "default"))

// ❌ 错误
.advisors(a -> a.param("chat_memory_conversation_id", "default"))
```

### 6.2 MCP Tool 注解规范

- 使用 `@Tool` 注解（`org.springframework.ai.tool.annotation.Tool`），而非 `@Tool` 的替代方案
- `name` 使用**蛇形命名法**（`snake_case`）
- `description` 写清楚工具功能，供 LLM 理解调用场景
- MCP Tool 类加 `@Component` 自动注册

```java
@Tool(name = "get_current_datetime", description = "查询当前的日期和时间")
public static String getCurrentDateTime() { ... }
```

### 6.3 日志规范

- 类级别加 `@Slf4j`
- 重要入参用 `log.debug("操作: {}", param)` 记录
- 异常用 `log.error("描述", exception)` 输出完整堆栈
- MCP 协议日志级别为 `DEBUG`

### 6.4 API 路径规范

- LLM 聊天：`/api/chat/q`, `/api/chat/streamq`
- 古诗推荐（结构化输出）：`/api/chat/poems`
- 聊天记忆管理：`/api/chat/memory`
- 知识库 RAG：`/rag/*`
- SearXNG 搜索：`/SearXNG/*`
- SSE 推送：`/api/chat/sse/*`

### 6.5 依赖注入

- 使用构造器注入（推荐）或 `@Resource(name = "beanName")` 具名注入
- 避免 `@Autowired` 字段注入

### 6.6 编码

- **全链路 UTF-8**：源文件、编译器、请求/响应、日志编码统一 UTF-8
- 文件路径含中文时使用 URL 编码

### 6.7 禁止注释

- AI 助手生成代码时**禁止添加 JavaDoc 或行内注释**
- 人维护时如需注释，应简洁且说明"为什么"而非"是什么"

---

## 七、MCP 开发规范

### 7.1 新增 MCP Server

1. 创建新 Maven 模块，引入对应 starter：
   - SSE 模式：`spring-ai-starter-mcp-server-webflux`
   - Stdio 模式：`spring-ai-starter-mcp-server` + `web-application-type: none`
2. `application.yaml` 配置：
   ```yaml
   spring:
     ai:
       mcp:
         server:
           sse-endpoint: /sse  # SSE 模式
   ```
3. 编写 `@Component` + `@Tool` 方法类
4. 在 `XxxConfig` 中将 Tool 类注册为 `MethodToolCallbackProvider`
5. 打包后在 `help/jars/` 中放置预制 JAR（供 mcp-client Stdio 连接）

### 7.2 新增 MCP 连接（Client 侧）

在 `mcp-client` 的 `application.yaml` 中配置：

```yaml
spring:
  ai:
    mcp:
      client:
        sse:
          connections:
            my-server:
              url: http://localhost:8083
              sse-endpoint: /sse
        stdio:
          connections:
            my-server:
              command: java
              args: -jar help/jars/my-server.jar
        streamable-http:
          connections:
            my-server:
              url: https://example.com/mcp
              endpoint: ${MY_API_KEY}
```

### 7.3 工具回调集成（Client 侧）

LLM Client 构造时通过 `ToolCallbackProvider` 注入工具：

```java
public XxxClient(XxxChatModel chatModel, ToolCallbackProvider toolCallbackProvider) {
    this.client = ChatClient.builder(chatModel)
            .defaultTools(toolCallbackProvider)
            .defaultAdvisors(...)
            .build();
}
```

---

## 八、测试规范

- 使用 Mockito 模拟外部依赖（MCP Client、LLM 等）
- 测试类中禁用 MCP 客户端：`spring.ai.mcp.client.enabled=false`
- 使用最小化 Spring 上下文（`@SpringBootTest` + 必要配置）
- 测试方法命名：`testXxx_shouldYyy_whenZzz`

---

## 九、Git 工作流规范

- 禁止提交 `.env` 文件、IDE 配置文件（`.idea/`、`*.iml`）
- 禁止提交编译产物（`target/`、`*.jar` 除 `help/jars/` 外）
- 提交信息格式：`type(scope): message`，如 `feat(mcp-server): 添加用户查询工具`
- 推送到远程前务必检查 `git status` 和 `git diff`

---

## 十、配置文件规范

### 10.1 application.yaml

- 敏感信息使用 `${ENV_VAR}` 占位符
- MCP 连接配置按传输类型分组（`sse.connections.*`、`stdio.connections.*`、`streamable-http.connections.*`）
- 模块级日志配置：`cn.netbuffer: DEBUG`
- 全链路 UTF-8 编码

### 10.2 环境变量

- 统一在模块 `src/main/resources/` 目录放置 `{module-name}.env` 文件
- 启动类通过 `DotEnvUtils.initDotEnv2SystemProperty("{module-name}.env")` 加载
- `.env` 目录已加入 `.gitignore`
- 环境变量模板见 `help/templates/mcp-client.template.env`
- 关键环境变量：
  - `OPENAI_API_KEY` — OpenAI API 密钥
  - `DEEPSEEK_API_KEY` — DeepSeek API 密钥
  - `DEEPSEEK_API_URL` — DeepSeek API 地址（默认 `https://api.deepseek.com`）
  - `BAIDU_MAP_API_KEY` — 百度地图 MCP API 参数（可选，默认注释）

---

## 十一、依赖管理规范

- 版本统一在父 `pom.xml` 的 `<properties>` 中声明
- 使用 `spring-ai-bom` 管理 Spring AI 版本
- 模块间依赖（如 `mcp-client` → `mcp-common`）使用 `${project.version}` 引用
- 新增依赖前检查是否存在版本冲突
