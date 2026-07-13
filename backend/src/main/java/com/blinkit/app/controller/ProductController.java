package com.blinkit.app.controller;

import com.blinkit.app.catalog.ProductCatalog;
import com.blinkit.app.model.Product;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductCatalog catalog;

    public ProductController(ProductCatalog catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/products")
    public List<Product> products(@RequestParam(required = false) String search,
                                  @RequestParam(required = false) String category) {
        if (search != null && !search.isBlank()) {
            return catalog.search(search);
        }
        if (category != null && !category.isBlank() && !"All".equalsIgnoreCase(category)) {
            return catalog.byCategory(category);
        }
        return catalog.all();
    }

    @GetMapping("/categories")
    public List<String> categories() {
        return catalog.categories();
    }
}
