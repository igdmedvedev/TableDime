package org.purchases.controllers;

import jakarta.validation.Valid;
import org.purchases.models.Purchase;
import org.purchases.services.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Book;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/purchases")
public class PurchaseController {
    final private PurchaseService purchaseService;

    @Autowired
    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping("/categories")
    @ResponseBody
    public List<String> getCategories() {
        return purchaseService.collectCategories();
    }

    @GetMapping
    public String showPurchases(Model model,
                                @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date", "category", "amt").descending());
        Page<Purchase> purchases = purchaseService.collectPurchases(pageable);

        model.addAttribute("purchases", purchases.getContent());
        model.addAttribute("userName", "userName");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", purchases.getTotalPages());

        return "purchases/index";
    }

    @PostMapping("/batch")
    public ResponseEntity<?> addBatchPurchases(@Valid @RequestBody List<Purchase> purchases, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(collectErrors(result));
        }

        purchaseService.saveAll(purchases);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> updatePurchase(@PathVariable Integer id, @Valid @RequestBody Purchase purchase, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(collectErrors(result));
        }

        purchase.setId(id);
        purchaseService.saveOrUpdate(purchase);
        return ResponseEntity.ok("Success");
    }

    @DeleteMapping("/{id}")
    public String deletePurchase(@PathVariable Integer id) {
        purchaseService.delete(id);
        return "redirect:/purchases";
    }

    private Map<String, String> collectErrors(BindingResult result) {
        return result.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
    }
}
