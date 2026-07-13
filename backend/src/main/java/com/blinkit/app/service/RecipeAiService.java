package com.blinkit.app.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.ThinkingConfigAdaptive;
import com.blinkit.app.catalog.ProductCatalog;
import com.blinkit.app.dto.RecipeSuggestion;
import com.blinkit.app.dto.RecipeSuggestion.SuggestedItem;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Turns a dish name into a shoppable ingredient list.
 * Uses Claude when an Anthropic API key is configured; otherwise falls back
 * to the built-in {@link RecipeKnowledgeBase}.
 */
@Service
public class RecipeAiService {

    private static final Logger log = LoggerFactory.getLogger(RecipeAiService.class);

    private static final String SYSTEM_PROMPT = """
            You are the ingredient planner for a Blinkit-style instant grocery delivery app.
            The user gives you a dish name. Choose every ingredient needed to cook it,
            picking ONLY from the store catalog provided in the user message.

            Respond with STRICT JSON only — no markdown, no code fences, no commentary — matching:
            {
              "recipeName": "proper display name of the dish",
              "description": "one appetizing sentence about the dish",
              "items": [
                {"productId": <int, must exist in the catalog>,
                 "quantity": <int >= 1, number of catalog units to buy>,
                 "note": "short tip on how this ingredient is used or prepped"}
              ],
              "missing": ["ingredients the dish needs that the store does not carry"]
            }

            Rules:
            - Quantities should be realistic for cooking the dish for 2-3 people.
            - Include basics like oil, salt and spices when the dish needs them.
            - Never invent product ids. If an ingredient is not in the catalog, put it in "missing".
            - If the input is not a real dish, return an empty items list and explain in the description.
            """;

    private final ProductCatalog catalog;
    private final RecipeKnowledgeBase knowledgeBase;
    private final String apiKey;
    private final ObjectMapper mapper = new ObjectMapper();
    private volatile AnthropicClient client;

    public RecipeAiService(ProductCatalog catalog, RecipeKnowledgeBase knowledgeBase,
                           @Value("${anthropic.api-key:}") String apiKey) {
        this.catalog = catalog;
        this.knowledgeBase = knowledgeBase;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public RecipeSuggestion suggest(String dish) {
        String trimmed = dish == null ? "" : dish.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Please tell us what you want to cook.");
        }
        if (apiKeyConfigured()) {
            try {
                return fromClaude(trimmed);
            } catch (Exception e) {
                log.warn("Claude request failed, using built-in recipe book instead: {}", e.toString());
            }
        }
        return knowledgeBase.suggest(trimmed);
    }

    private boolean apiKeyConfigured() {
        return !apiKey.isBlank();
    }

    private AnthropicClient client() {
        AnthropicClient c = client;
        if (c == null) {
            synchronized (this) {
                if (client == null) {
                    client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
                }
                c = client;
            }
        }
        return c;
    }

    private RecipeSuggestion fromClaude(String dish) throws Exception {
        String userMessage = "Store catalog (id | name | category | unit):\n"
                + catalog.asPromptCatalog()
                + "\nDish: " + dish;

        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.of("claude-opus-4-8"))
                .maxTokens(4096L)
                .thinking(ThinkingConfigAdaptive.builder().build())
                .system(SYSTEM_PROMPT)
                .addUserMessage(userMessage)
                .build();

        Message response = client().messages().create(params);
        String text = response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(t -> t.text())
                .collect(Collectors.joining());

        AiPayload payload = mapper.readValue(stripFences(text), AiPayload.class);

        // Keep only product ids that really exist; move the rest to "missing".
        List<SuggestedItem> items = new ArrayList<>();
        List<String> missing = new ArrayList<>(payload.missing() == null ? List.of() : payload.missing());
        if (payload.items() != null) {
            for (AiItem item : payload.items()) {
                if (catalog.byId(item.productId()).isPresent()) {
                    items.add(new SuggestedItem(item.productId(), Math.max(1, item.quantity()),
                            item.note() == null ? "" : item.note()));
                } else {
                    missing.add("Unknown item (id " + item.productId() + ")");
                }
            }
        }
        return new RecipeSuggestion(
                payload.recipeName() == null ? dish : payload.recipeName(),
                payload.description() == null ? "" : payload.description(),
                items, missing, "ai");
    }

    private static String stripFences(String text) {
        String t = text.trim();
        if (t.startsWith("```")) {
            int firstNewline = t.indexOf('\n');
            int lastFence = t.lastIndexOf("```");
            if (firstNewline >= 0 && lastFence > firstNewline) {
                t = t.substring(firstNewline + 1, lastFence).trim();
            }
        }
        return t;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AiPayload(String recipeName, String description, List<AiItem> items, List<String> missing) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AiItem(int productId, int quantity, String note) {
    }
}
