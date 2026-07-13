package com.blinkit.app.model;

public record Product(
        int id,
        String name,
        String category,
        String unit,
        double price,
        double mrp,
        String emoji,
        int deliveryMinutes
) {
}
