package org.purchases.services;

import jakarta.persistence.EntityNotFoundException;
import org.purchases.models.Purchase;
import org.purchases.repositories.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Page<Purchase> collectPurchases(Pageable pageable) {
        return purchaseRepository.findAll(pageable);
    }
    public List<Purchase> collectPurchases() {
        return purchaseRepository.findAll();
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
