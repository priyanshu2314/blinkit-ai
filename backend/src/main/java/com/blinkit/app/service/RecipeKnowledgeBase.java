package com.blinkit.app.service;

import com.blinkit.app.catalog.ProductCatalog;
import com.blinkit.app.dto.RecipeSuggestion;
import com.blinkit.app.dto.RecipeSuggestion.SuggestedItem;
import com.blinkit.app.model.Product;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Offline recipe knowledge base. Used when no Anthropic API key is configured
 * (or the API call fails) so the "Cook with AI" feature always works.
 */
@Component
public class RecipeKnowledgeBase {

    private record Ing(String productName, int quantity, String note) {
    }

    private record Recipe(String name, String description, List<String> aliases,
                          List<Ing> ingredients, List<String> missing) {
    }

    private final ProductCatalog catalog;
    private final List<Recipe> recipes;

    public RecipeKnowledgeBase(ProductCatalog catalog) {
        this.catalog = catalog;
        this.recipes = buildRecipes();
    }

    public RecipeSuggestion suggest(String dish) {
        String q = normalize(dish);

        for (Recipe r : recipes) {
            for (String alias : r.aliases()) {
                if (q.contains(alias) || alias.contains(q)) {
                    return toSuggestion(r);
                }
            }
        }
        return genericMatch(dish);
    }

    private RecipeSuggestion toSuggestion(Recipe r) {
        List<SuggestedItem> items = new ArrayList<>();
        List<String> missing = new ArrayList<>(r.missing());
        for (Ing ing : r.ingredients()) {
            catalog.search(ing.productName()).stream().findFirst().ifPresentOrElse(
                    p -> items.add(new SuggestedItem(p.id(), ing.quantity(), ing.note())),
                    () -> missing.add(ing.productName()));
        }
        return new RecipeSuggestion(r.name(), r.description(), items, missing, "fallback");
    }

    /** No known recipe — try to match dish words against product names. */
    private RecipeSuggestion genericMatch(String dish) {
        Set<Product> matched = new LinkedHashSet<>();
        for (String word : normalize(dish).split(" ")) {
            if (word.length() < 3) continue;
            matched.addAll(catalog.search(word));
        }
        List<SuggestedItem> items = matched.stream()
                .limit(8)
                .map(p -> new SuggestedItem(p.id(), 1, "Matched from your dish name"))
                .toList();
        String description = items.isEmpty()
                ? "We couldn't find a recipe for this dish. Try something like 'saute vegetables', 'paneer butter masala' or 'veg fried rice'."
                : "We don't have this exact recipe, but these items from our store match your dish.";
        return new RecipeSuggestion(capitalize(dish), description, items, List.of(), "fallback");
    }

    private static String normalize(String s) {
        // Fold accents (e.g. "sauté" -> "saute") before stripping non-letters.
        String folded = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return folded.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    private static String capitalize(String s) {
        String t = s.trim();
        return t.isEmpty() ? t : Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }

    private static List<Ing> ings(Ing... list) {
        return Arrays.asList(list);
    }

    private List<Recipe> buildRecipes() {
        return List.of(
                new Recipe("Sauté Vegetables",
                        "Crunchy mixed vegetables tossed in olive oil, garlic and cracked pepper — ready in 15 minutes.",
                        List.of("saute vegetable", "saute vegetables", "sauteed vegetables", "sauteed veggies",
                                "stir fry vegetables", "veg stir fry", "stir fried vegetables", "sauteed veg"),
                        ings(new Ing("Broccoli", 1, "Cut into small florets"),
                                new Ing("Carrot", 1, "Sliced diagonally"),
                                new Ing("French Beans", 1, "Cut into 1-inch pieces"),
                                new Ing("Capsicum (Green)", 1, "Cubed"),
                                new Ing("Zucchini", 1, "Half-moon slices"),
                                new Ing("Button Mushroom", 1, "Halved"),
                                new Ing("Baby Corn", 1, "Slit lengthwise"),
                                new Ing("Garlic", 1, "4-5 cloves, minced"),
                                new Ing("Extra Virgin Olive Oil", 1, "2 tbsp for tossing"),
                                new Ing("Salt", 1, "To taste"),
                                new Ing("Black Pepper Powder", 1, "Freshly cracked, to finish")),
                        List.of()),

                new Recipe("Veg Fried Rice",
                        "Restaurant-style fried rice with crunchy vegetables and soy sauce.",
                        List.of("veg fried rice", "fried rice", "vegetable fried rice", "chinese rice"),
                        ings(new Ing("Basmati Rice", 1, "2 cups, cooked and cooled"),
                                new Ing("Carrot", 1, "Finely chopped"),
                                new Ing("French Beans", 1, "Finely chopped"),
                                new Ing("Capsicum (Green)", 1, "Finely chopped"),
                                new Ing("Spring Onion", 1, "Whites for frying, greens for garnish"),
                                new Ing("Garlic", 1, "Minced"),
                                new Ing("Soy Sauce", 1, "1.5 tbsp"),
                                new Ing("White Vinegar", 1, "1 tsp"),
                                new Ing("Sunflower Oil", 1, "For high-heat tossing"),
                                new Ing("Salt", 1, "To taste"),
                                new Ing("Black Pepper Powder", 1, "1/2 tsp")),
                        List.of()),

                new Recipe("Paneer Butter Masala",
                        "Soft paneer cubes in a rich, creamy tomato-butter gravy.",
                        List.of("paneer butter masala", "butter paneer", "paneer makhani", "pbm"),
                        ings(new Ing("Fresh Paneer", 1, "Cubed, 200 g"),
                                new Ing("Tomato", 2, "For the gravy base"),
                                new Ing("Onion", 1, "1 large, for the gravy"),
                                new Ing("Garlic", 1, "5-6 cloves"),
                                new Ing("Ginger", 1, "1-inch piece"),
                                new Ing("Amul Butter", 1, "3 tbsp"),
                                new Ing("Fresh Cream", 1, "3 tbsp to finish"),
                                new Ing("Red Chilli Powder", 1, "Kashmiri, for colour"),
                                new Ing("Garam Masala", 1, "1/2 tsp"),
                                new Ing("Kasuri Methi", 1, "Crushed, to finish"),
                                new Ing("Salt", 1, "To taste"),
                                new Ing("Sugar", 1, "A pinch, balances the tomatoes")),
                        List.of("Cashew nuts (for richer gravy)")),

                new Recipe("White Sauce Pasta",
                        "Creamy béchamel pasta loaded with veggies.",
                        List.of("white sauce pasta", "pasta", "alfredo pasta", "creamy pasta"),
                        ings(new Ing("Penne Pasta", 1, "250 g, boiled al dente"),
                                new Ing("Amul Taaza Milk", 1, "2 cups for the sauce"),
                                new Ing("Amul Butter", 1, "For the roux"),
                                new Ing("Cheese Slices", 1, "2-3 slices, melted in"),
                                new Ing("Garlic", 1, "Minced"),
                                new Ing("Sweet Corn Kernels", 1, "1/2 cup"),
                                new Ing("Capsicum (Green)", 1, "Julienned"),
                                new Ing("Button Mushroom", 1, "Sliced"),
                                new Ing("Black Pepper Powder", 1, "Generous amount"),
                                new Ing("Salt", 1, "To taste")),
                        List.of("Plain flour / maida (for the roux)", "Oregano & chilli flakes")),

                new Recipe("Veg Hakka Noodles",
                        "Street-style tossed noodles with crunchy vegetables.",
                        List.of("hakka noodles", "noodles", "veg noodles", "chowmein", "chow mein"),
                        ings(new Ing("Hakka Noodles", 1, "Boiled and drained"),
                                new Ing("Cabbage", 1, "Shredded"),
                                new Ing("Carrot", 1, "Julienned"),
                                new Ing("Capsicum (Green)", 1, "Julienned"),
                                new Ing("Spring Onion", 1, "Chopped"),
                                new Ing("Garlic", 1, "Minced"),
                                new Ing("Soy Sauce", 1, "1.5 tbsp"),
                                new Ing("Green Chilli Sauce", 1, "1 tbsp"),
                                new Ing("White Vinegar", 1, "1 tsp"),
                                new Ing("Sunflower Oil", 1, "For tossing on high flame"),
                                new Ing("Salt", 1, "To taste"),
                                new Ing("Black Pepper Powder", 1, "1/2 tsp")),
                        List.of()),

                new Recipe("Dal Tadka",
                        "Comforting toor dal finished with a ghee-cumin tadka.",
                        List.of("dal tadka", "dal", "daal", "yellow dal", "dal fry"),
                        ings(new Ing("Toor Dal", 1, "1 cup, pressure cooked"),
                                new Ing("Onion", 1, "Finely chopped"),
                                new Ing("Tomato", 1, "Finely chopped"),
                                new Ing("Garlic", 1, "For the tadka"),
                                new Ing("Ginger", 1, "Grated"),
                                new Ing("Green Chilli", 1, "Slit"),
                                new Ing("Cumin Seeds (Jeera)", 1, "1 tsp for the tadka"),
                                new Ing("Turmeric Powder", 1, "1/2 tsp"),
                                new Ing("Red Chilli Powder", 1, "1/2 tsp"),
                                new Ing("Pure Cow Ghee", 1, "2 tbsp for the tadka"),
                                new Ing("Coriander Leaves", 1, "For garnish"),
                                new Ing("Salt", 1, "To taste")),
                        List.of()),

                new Recipe("Masala Omelette",
                        "Fluffy eggs with onion, chilli and coriander.",
                        List.of("omelette", "omelet", "masala omelette", "egg omelette"),
                        ings(new Ing("Farm Eggs", 1, "3 eggs"),
                                new Ing("Onion", 1, "Finely chopped"),
                                new Ing("Tomato", 1, "Finely chopped"),
                                new Ing("Green Chilli", 1, "Finely chopped"),
                                new Ing("Coriander Leaves", 1, "Chopped"),
                                new Ing("Amul Butter", 1, "For the pan"),
                                new Ing("Salt", 1, "To taste"),
                                new Ing("Black Pepper Powder", 1, "A pinch")),
                        List.of()),

                new Recipe("Aloo Paratha",
                        "Stuffed potato flatbread — best with butter and curd.",
                        List.of("aloo paratha", "paratha", "aalu paratha", "potato paratha"),
                        ings(new Ing("Potato", 1, "Boiled and mashed"),
                                new Ing("Whole Wheat Atta", 1, "For the dough"),
                                new Ing("Green Chilli", 1, "Finely chopped"),
                                new Ing("Coriander Leaves", 1, "Chopped into the filling"),
                                new Ing("Cumin Seeds (Jeera)", 1, "Roasted, crushed"),
                                new Ing("Red Chilli Powder", 1, "1/2 tsp"),
                                new Ing("Pure Cow Ghee", 1, "For roasting"),
                                new Ing("Curd (Dahi)", 1, "To serve"),
                                new Ing("Amul Butter", 1, "A cube on top"),
                                new Ing("Salt", 1, "To taste")),
                        List.of("Ajwain (carom seeds)")),

                new Recipe("Kanda Poha",
                        "Light Maharashtrian breakfast of flattened rice with onions.",
                        List.of("poha", "kanda poha", "pohe"),
                        ings(new Ing("Poha (Flattened Rice)", 1, "Rinsed and drained"),
                                new Ing("Onion", 1, "Finely chopped"),
                                new Ing("Potato", 1, "Small cubes (optional)"),
                                new Ing("Green Chilli", 1, "Chopped"),
                                new Ing("Turmeric Powder", 1, "1/2 tsp"),
                                new Ing("Sugar", 1, "1 tsp, signature touch"),
                                new Ing("Lemon", 1, "Juice of half"),
                                new Ing("Coriander Leaves", 1, "For garnish"),
                                new Ing("Sunflower Oil", 1, "2 tbsp"),
                                new Ing("Salt", 1, "To taste")),
                        List.of("Curry leaves", "Roasted peanuts")),

                new Recipe("Chicken Curry",
                        "Homestyle chicken curry with onion-tomato gravy.",
                        List.of("chicken curry", "chicken masala", "chicken gravy", "murgh curry"),
                        ings(new Ing("Chicken Curry Cut", 1, "500 g"),
                                new Ing("Onion", 1, "2 large, sliced"),
                                new Ing("Tomato", 1, "Pureed"),
                                new Ing("Garlic", 1, "Paste"),
                                new Ing("Ginger", 1, "Paste"),
                                new Ing("Curd (Dahi)", 1, "For marination"),
                                new Ing("Turmeric Powder", 1, "1/2 tsp"),
                                new Ing("Red Chilli Powder", 1, "1 tsp"),
                                new Ing("Garam Masala", 1, "1 tsp"),
                                new Ing("Coriander Leaves", 1, "For garnish"),
                                new Ing("Sunflower Oil", 1, "3 tbsp"),
                                new Ing("Salt", 1, "To taste")),
                        List.of()),

                new Recipe("Palak Paneer",
                        "Silky spinach gravy with soft paneer cubes.",
                        List.of("palak paneer", "spinach paneer", "saag paneer"),
                        ings(new Ing("Spinach (Palak)", 2, "Blanched and pureed"),
                                new Ing("Fresh Paneer", 1, "Cubed"),
                                new Ing("Onion", 1, "Finely chopped"),
                                new Ing("Tomato", 1, "Finely chopped"),
                                new Ing("Garlic", 1, "Minced"),
                                new Ing("Ginger", 1, "Grated"),
                                new Ing("Green Chilli", 1, "1-2, to taste"),
                                new Ing("Fresh Cream", 1, "A swirl to finish"),
                                new Ing("Garam Masala", 1, "1/2 tsp"),
                                new Ing("Pure Cow Ghee", 1, "For tempering"),
                                new Ing("Salt", 1, "To taste")),
                        List.of()),

                new Recipe("Grilled Veg Sandwich",
                        "Toasty sandwich with crunchy veggies and melted cheese.",
                        List.of("sandwich", "grilled sandwich", "veg sandwich", "cheese sandwich"),
                        ings(new Ing("Bread (Whole Wheat)", 1, "4-6 slices"),
                                new Ing("Amul Butter", 1, "Spread generously"),
                                new Ing("Cheese Slices", 1, "1 per sandwich"),
                                new Ing("Tomato", 1, "Thin rounds"),
                                new Ing("Onion", 1, "Thin rounds"),
                                new Ing("Capsicum (Green)", 1, "Thin rounds"),
                                new Ing("Tomato Ketchup", 1, "To serve"),
                                new Ing("Black Pepper Powder", 1, "Sprinkle"),
                                new Ing("Salt", 1, "To taste")),
                        List.of("Green chutney")),

                new Recipe("Tomato Soup",
                        "Velvety tomato soup with a touch of cream.",
                        List.of("tomato soup", "soup"),
                        ings(new Ing("Tomato", 2, "6-7 ripe tomatoes"),
                                new Ing("Garlic", 1, "3-4 cloves"),
                                new Ing("Onion", 1, "1 small"),
                                new Ing("Amul Butter", 1, "1 tbsp"),
                                new Ing("Fresh Cream", 1, "To finish"),
                                new Ing("Cornflour", 1, "1 tsp to thicken"),
                                new Ing("Sugar", 1, "A pinch"),
                                new Ing("Black Pepper Powder", 1, "To taste"),
                                new Ing("Salt", 1, "To taste")),
                        List.of("Bread croutons")),

                new Recipe("Veg Pulao",
                        "One-pot fragrant rice with mixed vegetables.",
                        List.of("veg pulao", "pulao", "pulav", "vegetable rice"),
                        ings(new Ing("Basmati Rice", 1, "1.5 cups, soaked"),
                                new Ing("Green Peas", 1, "1/2 cup"),
                                new Ing("Carrot", 1, "Diced"),
                                new Ing("French Beans", 1, "Chopped"),
                                new Ing("Potato", 1, "Diced (optional)"),
                                new Ing("Onion", 1, "Sliced"),
                                new Ing("Ginger", 1, "Julienned"),
                                new Ing("Cumin Seeds (Jeera)", 1, "1 tsp"),
                                new Ing("Garam Masala", 1, "1/2 tsp"),
                                new Ing("Pure Cow Ghee", 1, "2 tbsp"),
                                new Ing("Coriander Leaves", 1, "For garnish"),
                                new Ing("Salt", 1, "To taste")),
                        List.of("Whole spices (bay leaf, cinnamon, cloves)"))
        );
    }
}
