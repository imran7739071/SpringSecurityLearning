package com.imri.spring_security.repository;

import com.imri.spring_security.entity.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ── Way 1: Method name derivation ─────────────────────────────────
    List<Product> findByCategory(String category);
    List<Product> findByPriceBetween(Double min, Double max);
    List<Product> findByNameContainingIgnoreCase(String keyword);
    List<Product> findByCategoryAndPriceLessThan(String category, Double price);
    List<Product> findAllByOrderByPriceAsc();
    List<Product> findAllByOrderByCreatedAtDesc();
    List<Product> findByCreatedById(Long userId);
    List<Product> findTop5ByCategoryOrderByCreatedAtDesc(String category);
    Long countByCategory(String category);
    Boolean existsByName(String name);

    // ── Way 2: JPQL queries ───────────────────────────────────────────
    @Query("SELECT p FROM Product p JOIN p.createdBy u WHERE u.username = :username")
    List<Product> findByCreatedByUsername(@Param("username") String username);

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :min AND :max " +
            "AND p.category = :category ORDER BY p.price ASC")
    List<Product> findByCategoryAndPriceRange(
            @Param("category") String category,
            @Param("min") Double min,
            @Param("max") Double max);

    @Query("SELECT AVG(p.price) FROM Product p WHERE p.category = :category")
    Double averagePriceByCategory(@Param("category") String category);

    @Query("SELECT p FROM Product p WHERE p.quantity <= :threshold ORDER BY p.quantity ASC")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    // ── Way 3: Native SQL ─────────────────────────────────────────────
    @Query(value = """
            SELECT category,
                   COUNT(*) as total,
                   AVG(price) as avg_price,
                   SUM(quantity) as total_stock
            FROM products
            GROUP BY category
            ORDER BY total DESC
            """, nativeQuery = true)
    List<Object[]> getCategoryStats();

    // ── Way 4: Modifying queries ──────────────────────────────────────
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.quantity = :quantity WHERE p.id = :id")
    int updateQuantity(@Param("id") Long id,
                       @Param("quantity") Integer quantity);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.price = p.price * :multiplier " +
            "WHERE p.category = :category")
    int applyPriceMultiplierToCategory(
            @Param("multiplier") Double multiplier,
            @Param("category") String category);

    // ── Way 5: Pagination ─────────────────────────────────────────────
    Page<Product> findByCategory(String category, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchPaginated(@Param("keyword") String keyword,
                                  Pageable pageable);
}