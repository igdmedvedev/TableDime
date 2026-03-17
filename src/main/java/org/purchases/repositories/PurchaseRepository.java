package org.purchases.repositories;

import org.purchases.models.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase,Integer> {
    @Query("SELECT p.category FROM Purchase p GROUP BY p.category ORDER BY count(*) DESC")
    List<String> findAllUniqueCategories();
}
