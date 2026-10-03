package com.example.livemart.repository;

import com.example.livemart.model.Order;
import com.example.livemart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderIdAndSoldBy(Long orderId, User soldBy);

    List<Order> findAllByBoughtBy(User boughtBy);
}
