package org.purchases.controllers;

import jakarta.validation.Valid;
import org.purchases.models.Purchase;
import org.purchases.repositories.PurchaseRepository;
import org.purchases.services.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                @RequestParam(required = false) String category,
                                @RequestParam(required = false) String comments) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date", "category", "amt").descending());
        Page<Purchase> purchases = purchaseService.collectPurchases(pageable, startDate, endDate, category, comments);

        model.addAttribute("purchases", purchases.getContent());
        model.addAttribute("userName", "userName");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", purchases.getTotalPages());

        return "purchases/index";
    }

    @GetMapping("/spending-by-absolute-period")
    @ResponseBody
    public List<PurchaseRepository.CategorySpending> getSpendingByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return purchaseService.collectSpendingByPeriod(startDate, endDate);
    }

    @GetMapping("/spending-by-relative-period")
    @ResponseBody
    public List<PurchaseRepository.CategorySpending> getSpendingByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anchor,
            @RequestParam Integer page) {
        return purchaseService.collectSpendingByPeriod(anchor, page);
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
    @ResponseBody
    public ResponseEntity<?> deletePurchase(@PathVariable Integer id) {
        purchaseService.delete(id);
        return ResponseEntity.ok("Success");
    }

    private Map<String, String> collectErrors(BindingResult result) {
        return result.getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
    }
}
