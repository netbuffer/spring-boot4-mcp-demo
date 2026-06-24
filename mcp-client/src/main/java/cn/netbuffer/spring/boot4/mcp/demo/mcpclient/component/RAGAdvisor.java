package cn.netbuffer.spring.boot4.mcp.demo.mcpclient.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.template.ValidationMode;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class RAGAdvisor implements BaseAdvisor {

    private static final int RAG_CONTEXT_RESULT_COUNT = 5;

    private final VectorStore vectorStore;
    private final PromptTemplate ragPromptTemplate;
    private final int order;

    public RAGAdvisor(VectorStore vectorStore) {
        this(vectorStore, Ordered.HIGHEST_PRECEDENCE + 900);
    }

    public RAGAdvisor(VectorStore vectorStore, int order) {
        this.vectorStore = vectorStore;
        StTemplateRenderer renderer = StTemplateRenderer.builder()
                .validationMode(ValidationMode.NONE)
                .build();
        this.ragPromptTemplate = PromptTemplate.builder()
                .resource(new ClassPathResource("prompts/rag-system.st"))
                .renderer(renderer)
                .build();
        this.order = order;
    }

    @Override
    public String getName() {
        return "RAGAdvisor";
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        String userQuery = extractUserQuery(request);
        if (userQuery == null || userQuery.isBlank()) {
            return request;
        }

        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query(userQuery).topK(RAG_CONTEXT_RESULT_COUNT).build()
        );

        String context;
        if (docs.isEmpty()) {
            log.debug("RAGAdvisor found no relevant documents");
            context = "";
        } else {
            context = docs.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n\n---\n\n"));
            log.debug("RAGAdvisor found {} docs, context length={}", docs.size(), context.length());
        }

        boolean hasContext = !docs.isEmpty();
        String ragSystem = ragPromptTemplate.render(Map.of("hasContext", hasContext, "context", context));
        Prompt augmentedPrompt = request.prompt().augmentSystemMessage(
                sm -> sm.mutate().text(sm.getText() + "\n\n" + ragSystem).build()
        );

        return request.mutate().prompt(augmentedPrompt).build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        return response;
    }

    private String extractUserQuery(ChatClientRequest request) {
        if (request.prompt().getUserMessage() != null) {
            return request.prompt().getUserMessage().getText();
        }
        return "";
    }

}
