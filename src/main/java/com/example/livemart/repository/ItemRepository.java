package com.example.livemart.repository;

import com.example.livemart.model.Item;
import com.example.livemart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByCategory(String categoryName);

    List<Item> findAllBySoldByAndCategoryOrderByPriceAsc(User soldBy, String category);
    List<Item> findAllBySoldByAndCategoryOrderByPriceDesc(User soldBy, String category);

    List<Item> findAllBySoldBy(User soldBy);

    List<Item> findAllByOrderByCategoryAsc();

    List<Item> findAllByOrderByCategoryDesc();

    Optional<Item> findByIdAndSoldBy(Long itemId, User user);

    @Query(
            value = "SELECT * FROM items " +
                    "WHERE similarity(item_name, :query) > 0.3 " +
            "ORDER BY similarity(item_name, :query) DESC",
            nativeQuery = true
    )
    List<Item> searchSimilar(@Param("query") String query);
}
