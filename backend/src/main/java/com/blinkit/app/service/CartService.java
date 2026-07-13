package com.blinkit.app.service;

import com.blinkit.app.catalog.ProductCatalog;
import com.blinkit.app.model.Product;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    public record CartLine(Product product, int quantity, double lineTotal) {
    }

    public record CartView(List<CartLine> items, int itemCount, double subtotal, double savings) {
    }

    private final ProductCatalog catalog;
    private final Map<Integer, Integer> quantities = new LinkedHashMap<>();

    public CartService(ProductCatalog catalog) {
        this.catalog = catalog;
    }

    public synchronized CartView view() {
        List<CartLine> lines = quantities.entrySet().stream()
                .map(e -> {
                    Product p = catalog.byId(e.getKey()).orElseThrow();
                    return new CartLine(p, e.getValue(), round(p.price() * e.getValue()));
                })
                .toList();
        int count = lines.stream().mapToInt(CartLine::quantity).sum();
        double subtotal = round(lines.stream().mapToDouble(CartLine::lineTotal).sum());
        double mrpTotal = lines.stream().mapToDouble(l -> l.product().mrp() * l.quantity()).sum();
        return new CartView(lines, count, subtotal, round(mrpTotal - subtotal));
    }

    public synchronized CartView add(int productId, int quantity) {
        catalog.byId(productId).orElseThrow(() -> new IllegalArgumentException("Unknown product: " + productId));
        quantities.merge(productId, Math.max(1, quantity), Integer::sum);
        return view();
    }

    public synchronized CartView setQuantity(int productId, int quantity) {
        if (quantity <= 0) {
            quantities.remove(productId);
        } else {
            catalog.byId(productId).orElseThrow(() -> new IllegalArgumentException("Unknown product: " + productId));
            quantities.put(productId, quantity);
        }
        return view();
    }

    public synchronized CartView clear() {
        quantities.clear();
        return view();
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
