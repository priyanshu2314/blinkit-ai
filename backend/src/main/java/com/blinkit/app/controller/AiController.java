package com.blinkit.app.controller;

import com.blinkit.app.dto.RecipeSuggestion;
import com.blinkit.app.service.RecipeAiService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    public record SuggestRequest(String dish) {
    }

    private final RecipeAiService recipeAiService;

    public AiController(RecipeAiService recipeAiService) {
        this.recipeAiService = recipeAiService;
    }

    @PostMapping("/suggest-ingredients")
    public RecipeSuggestion suggest(@RequestBody SuggestRequest request) {
        return recipeAiService.suggest(request.dish());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> badRequest(IllegalArgumentException e) {
        return Map.of("error", e.getMessage());
    }
}
