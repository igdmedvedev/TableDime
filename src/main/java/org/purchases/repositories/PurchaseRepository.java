package org.purchases.repositories;

import org.purchases.models.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase,Integer>, JpaSpecificationExecutor<Purchase> {
    @Query("SELECT p.category " +
            "FROM Purchase p " +
            "GROUP BY p.category " +
            "ORDER BY count(*) DESC")
    List<String> findAllUniqueCategories();

    interface CategorySpending {
        String getCategory();
        Double getAmt();
    }
    @Query("SELECT p.category AS category, SUM(p.amt) AS amt " +
            "FROM Purchase p " +
            "WHERE p.date >= :start AND p.date <= :end " +
            "GROUP BY p.category " +
            "ORDER BY sum(p.amt)")
    List<CategorySpending> findCategorySpendingByPeriod(@Param("start") LocalDate start, @Param("end") LocalDate end);

    interface DateSpending {
        LocalDate getDay();
        Double getAmt();
    }
    @Query("SELECT p.date AS day, SUM(p.amt) AS amt " +
            "FROM Purchase p " +
            "WHERE p.date >= :start AND p.date <= :end " +
            "GROUP BY p.date " +
            "ORDER BY sum(p.amt)")
    Page<DateSpending> findDateSpendingByPeriod(@Param("start") LocalDate start, @Param("end") LocalDate end, Pageable pageable);

    Page<Purchase> findByDateBetweenAndCategoryIn(LocalDate startDate, LocalDate endDate, List<String> categories, Pageable pageable);
}
