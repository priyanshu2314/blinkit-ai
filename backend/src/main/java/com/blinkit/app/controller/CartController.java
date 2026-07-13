package com.blinkit.app.controller;

import com.blinkit.app.service.CartService;
import com.blinkit.app.service.CartService.CartView;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    public record AddItemRequest(int productId, Integer quantity) {
    }

    public record UpdateQuantityRequest(int quantity) {
    }

    public record AddAllRequest(List<AddItemRequest> items) {
    }

    private final CartService cart;

    public CartController(CartService cart) {
        this.cart = cart;
    }

    @GetMapping
    public CartView view() {
        return cart.view();
    }

    @PostMapping("/items")
    public CartView add(@RequestBody AddItemRequest request) {
        return cart.add(request.productId(), request.quantity() == null ? 1 : request.quantity());
    }

    @PostMapping("/add-all")
    public CartView addAll(@RequestBody AddAllRequest request) {
        CartView view = cart.view();
        if (request.items() != null) {
            for (AddItemRequest item : request.items()) {
                view = cart.add(item.productId(), item.quantity() == null ? 1 : item.quantity());
            }
        }
        return view;
    }

    @PatchMapping("/items/{productId}")
    public CartView setQuantity(@PathVariable int productId, @RequestBody UpdateQuantityRequest request) {
        return cart.setQuantity(productId, request.quantity());
    }

    @DeleteMapping
    public CartView clear() {
        return cart.clear();
    }
}
