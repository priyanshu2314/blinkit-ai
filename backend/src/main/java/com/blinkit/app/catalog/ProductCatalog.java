package com.blinkit.app.catalog;

import com.blinkit.app.model.Product;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class ProductCatalog {

    private final List<Product> products = List.of(
            // Vegetables
            new Product(1, "Onion", "Vegetables", "1 kg", 32, 40, "🧅", 10),
            new Product(2, "Tomato", "Vegetables", "500 g", 28, 35, "🍅", 10),
            new Product(3, "Potato", "Vegetables", "1 kg", 36, 45, "🥔", 10),
            new Product(4, "Capsicum (Green)", "Vegetables", "250 g", 24, 30, "🫑", 10),
            new Product(5, "Carrot", "Vegetables", "500 g", 30, 38, "🥕", 10),
            new Product(6, "French Beans", "Vegetables", "250 g", 26, 32, "🫛", 10),
            new Product(7, "Broccoli", "Vegetables", "1 pc (approx 300 g)", 55, 70, "🥦", 10),
            new Product(8, "Zucchini", "Vegetables", "500 g", 48, 60, "🥒", 10),
            new Product(9, "Button Mushroom", "Vegetables", "200 g", 45, 55, "🍄", 10),
            new Product(10, "Garlic", "Vegetables", "200 g", 38, 48, "🧄", 10),
            new Product(11, "Ginger", "Vegetables", "200 g", 30, 36, "🫚", 10),
            new Product(12, "Green Chilli", "Vegetables", "100 g", 12, 15, "🌶️", 10),
            new Product(13, "Coriander Leaves", "Vegetables", "100 g", 15, 20, "🌿", 10),
            new Product(14, "Spinach (Palak)", "Vegetables", "250 g", 22, 28, "🥬", 10),
            new Product(15, "Cauliflower", "Vegetables", "1 pc", 40, 50, "🥦", 10),
            new Product(16, "Green Peas", "Vegetables", "500 g", 60, 75, "🟢", 10),
            new Product(17, "Sweet Corn Kernels", "Vegetables", "200 g", 35, 45, "🌽", 10),
            new Product(18, "Baby Corn", "Vegetables", "200 g", 42, 52, "🌽", 10),
            new Product(19, "Spring Onion", "Vegetables", "100 g", 18, 24, "🧅", 10),
            new Product(20, "Cabbage", "Vegetables", "1 pc", 35, 42, "🥬", 10),

            // Fruits
            new Product(21, "Banana (Robusta)", "Fruits", "6 pcs", 40, 48, "🍌", 10),
            new Product(22, "Apple (Shimla)", "Fruits", "4 pcs", 120, 150, "🍎", 10),
            new Product(23, "Lemon", "Fruits", "4 pcs", 20, 25, "🍋", 10),

            // Dairy & Eggs
            new Product(24, "Amul Taaza Milk", "Dairy & Eggs", "500 ml", 27, 27, "🥛", 10),
            new Product(25, "Amul Butter", "Dairy & Eggs", "100 g", 60, 62, "🧈", 10),
            new Product(26, "Fresh Paneer", "Dairy & Eggs", "200 g", 85, 95, "🧀", 10),
            new Product(27, "Curd (Dahi)", "Dairy & Eggs", "400 g", 35, 40, "🥣", 10),
            new Product(28, "Cheese Slices", "Dairy & Eggs", "10 slices", 130, 145, "🧀", 10),
            new Product(29, "Fresh Cream", "Dairy & Eggs", "200 ml", 65, 75, "🥛", 10),
            new Product(30, "Farm Eggs", "Dairy & Eggs", "6 pcs", 48, 54, "🥚", 10),

            // Staples & Grains
            new Product(31, "Basmati Rice", "Staples", "1 kg", 145, 180, "🍚", 10),
            new Product(32, "Whole Wheat Atta", "Staples", "5 kg", 240, 280, "🌾", 10),
            new Product(33, "Toor Dal", "Staples", "1 kg", 160, 185, "🫘", 10),
            new Product(34, "Poha (Flattened Rice)", "Staples", "500 g", 45, 55, "🍚", 10),
            new Product(35, "Penne Pasta", "Staples", "500 g", 95, 110, "🍝", 10),
            new Product(36, "Hakka Noodles", "Staples", "400 g", 55, 65, "🍜", 10),
            new Product(37, "Bread (Whole Wheat)", "Staples", "400 g", 45, 50, "🍞", 10),
            new Product(38, "Cornflour", "Staples", "500 g", 55, 65, "🌽", 10),
            new Product(55, "Sugar", "Staples", "1 kg", 48, 55, "🫙", 10),

            // Oils & Ghee
            new Product(39, "Sunflower Oil", "Oils & Ghee", "1 L", 145, 165, "🛢️", 10),
            new Product(40, "Extra Virgin Olive Oil", "Oils & Ghee", "500 ml", 425, 499, "🫒", 10),
            new Product(41, "Pure Cow Ghee", "Oils & Ghee", "500 ml", 320, 360, "🧈", 10),

            // Spices & Condiments
            new Product(42, "Salt (Iodised)", "Spices & Condiments", "1 kg", 24, 28, "🧂", 10),
            new Product(43, "Black Pepper Powder", "Spices & Condiments", "100 g", 90, 105, "🫙", 10),
            new Product(44, "Cumin Seeds (Jeera)", "Spices & Condiments", "100 g", 45, 55, "🫙", 10),
            new Product(45, "Turmeric Powder", "Spices & Condiments", "200 g", 48, 58, "🫙", 10),
            new Product(46, "Red Chilli Powder", "Spices & Condiments", "200 g", 65, 78, "🫙", 10),
            new Product(47, "Garam Masala", "Spices & Condiments", "100 g", 72, 85, "🫙", 10),
            new Product(48, "Soy Sauce", "Spices & Condiments", "200 ml", 55, 65, "🍶", 10),
            new Product(49, "White Vinegar", "Spices & Condiments", "500 ml", 40, 48, "🍶", 10),
            new Product(50, "Tomato Ketchup", "Spices & Condiments", "500 g", 95, 110, "🍅", 10),
            new Product(51, "Green Chilli Sauce", "Spices & Condiments", "200 g", 48, 58, "🍶", 10),
            new Product(52, "Kasuri Methi", "Spices & Condiments", "50 g", 35, 42, "🌿", 10),

            // Meat
            new Product(53, "Chicken Breast (Boneless)", "Meat & Fish", "450 g", 210, 240, "🍗", 15),
            new Product(54, "Chicken Curry Cut", "Meat & Fish", "500 g", 180, 205, "🍗", 15)
    );

    public List<Product> all() {
        return products;
    }

    public Optional<Product> byId(int id) {
        return products.stream().filter(p -> p.id() == id).findFirst();
    }

    public List<Product> search(String query) {
        String q = query.toLowerCase(Locale.ROOT).trim();
        return products.stream()
                .filter(p -> p.name().toLowerCase(Locale.ROOT).contains(q)
                        || p.category().toLowerCase(Locale.ROOT).contains(q))
                .toList();
    }

    public List<Product> byCategory(String category) {
        return products.stream()
                .filter(p -> p.category().equalsIgnoreCase(category))
                .toList();
    }

    public List<String> categories() {
        return products.stream().map(Product::category).distinct().toList();
    }

    /** Compact catalog listing used inside the AI prompt. */
    public String asPromptCatalog() {
        StringBuilder sb = new StringBuilder();
        for (Product p : products) {
            sb.append(p.id()).append(" | ").append(p.name())
              .append(" | ").append(p.category())
              .append(" | ").append(p.unit()).append('\n');
        }
        return sb.toString();
    }
}
