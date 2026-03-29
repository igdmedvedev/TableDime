package org.purchases.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import org.purchases.models.Purchase;
import org.purchases.repositories.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;

    @Autowired
    public PurchaseService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    public Page<Purchase> collectPurchases(
            Pageable pageable,
            LocalDate startDate,
            LocalDate endDate,
            String category,
            String comment) {
        return purchaseRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), endDate));
            }
            if (category != null && !category.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("category")), "%" + category.toLowerCase() + "%"));
            }
            if (comment != null && !comment.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("comments")), "%" + comment.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }
    public Page<Purchase> collectPurchases(Pageable pageable) {
        return purchaseRepository.findAll(pageable);
    }
    public List<Purchase> collectPurchases() {
        return purchaseRepository.findAll();
    }

    public List<PurchaseRepository.CategorySpending> collectSpendingByPeriod(LocalDate start, LocalDate end) {
        return purchaseRepository.findCategorySpendingByPeriod(start, end);
    }

    public List<PurchaseRepository.CategorySpending> collectSpendingByPeriod(LocalDate anchor, Integer page) {
        LocalDate start = anchor.minusMonths(page);
        LocalDate end = anchor.minusDays(1).minusMonths(page - 1);
        return purchaseRepository.findCategorySpendingByPeriod(start, end);
    }

    public Purchase loadByPk(Integer id) {
        Optional<Purchase> purchase = purchaseRepository.findById(id);
        if (purchase.isEmpty()) {
            throw new EntityNotFoundException(String.format("Purchase with id = %d wasn't found.", id));
        }
        return purchase.get();
    }

    public List<String> collectCategories() {
        return purchaseRepository.findAllUniqueCategories();
    }

    @Transactional
    public void saveOrUpdate(Purchase purchase) {
        purchaseRepository.save(purchase);
    }

    @Transactional
    public void saveAll(List<Purchase> purchase) {
        purchaseRepository.saveAll(purchase);
    }

    @Transactional
    public void delete(Integer id) {
        purchaseRepository.deleteById(id);
    }

    @Transactional
    public void delete(Purchase purchase) {
        purchaseRepository.delete(purchase);
    }
}
