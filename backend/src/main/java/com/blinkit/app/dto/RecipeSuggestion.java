package com.blinkit.app.dto;

import java.util.List;

public record RecipeSuggestion(
        String recipeName,
        String description,
        List<SuggestedItem> items,
        List<String> missing,
        String source
) {
    public record SuggestedItem(int productId, int quantity, String note) {
    }
}
