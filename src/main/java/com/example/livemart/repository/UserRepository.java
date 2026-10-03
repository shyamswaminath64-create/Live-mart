package com.example.livemart.repository;

import com.example.livemart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository <User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    @Query(value =
            "SELECT * FROM users " +
                    "WHERE similarity(store_name, :query) > 0.3 " +
                    "ORDER BY similarity(store_name, :query) DESC",
            nativeQuery = true)
    List<User> fuzzySearchStoreName(@Param("query") String query);
}
