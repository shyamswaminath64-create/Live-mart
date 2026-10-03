package com.example.livemart.service;

import com.example.livemart.model.Item;
import com.example.livemart.model.Order;
import com.example.livemart.model.User;
import com.example.livemart.model.enums.OrderStatus;
import com.example.livemart.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void save(Order order) {
        orderRepository.save(order);
    }

    public void update(Order order){
        orderRepository.save(order);
    }

    public boolean delete(Long orderId) {
        Order order = getOrderById(orderId);
        orderRepository.delete(order);
        return true;
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    public Optional<Order> getOrderByIdAndSoldByUser(Long orderId, User user){
        return orderRepository.findByOrderIdAndSoldBy(orderId, user);
    }

    public List<Order> getAllOrdersBoughtByUser(User user){
        return orderRepository.findAllByBoughtBy(user);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public OrderStatus getOrderStatus(Long orderId) {
        return getOrderById(orderId).getOrderStatus();
    }

    public boolean setOrderStatus(Long orderId, OrderStatus orderStatus) {
        Order currentOrder = getOrderById(orderId);
        currentOrder.setOrderStatus(orderStatus);
        orderRepository.save(currentOrder);
        return true;
    }

    public User getSeller(Long orderId) {
        return getOrderById(orderId).getSoldBy();
    }

    public boolean setSeller(Long orderId, User user) {
        Order currentOrder = getOrderById(orderId);
        currentOrder.setSoldBy(user);
        orderRepository.save(currentOrder);
        return true;
    }

    public User getBuyer(Long orderId) {
        return getOrderById(orderId).getBoughtBy();
    }

    public boolean setBuyer(Long orderId, User user) {
        Order currentOrder = getOrderById(orderId);
        currentOrder.setBoughtBy(user);
        orderRepository.save(currentOrder);
        return true;
    }

    public Item getOrderItem(Long orderId) {
        return getOrderById(orderId).getOrderedItem();
    }

    public boolean setOrderItem(Long orderId, Item item) {
        Order currentOrder = getOrderById(orderId);
        currentOrder.setOrderedItem(item);
        orderRepository.save(currentOrder);
        return true;
    }

    public Integer getOrderQuantity(Long orderId) {
        return getOrderById(orderId).getOrderQuantity();
    }

    public boolean setOrderQuantity(Long orderId, Integer quantity) {
        Order currentOrder = getOrderById(orderId);
        currentOrder.setOrderQuantity(quantity);
        orderRepository.save(currentOrder);
        return true;
    }

    public LocalDateTime getOrderDateTime(Long orderId) {
        return getOrderById(orderId).getOrderCreatedAt();
    }

    public boolean setOrderDateTime(Long orderId, LocalDateTime dateTime) {
        Order currentOrder = getOrderById(orderId);
        currentOrder.setOrderCreatedAt(dateTime);
        orderRepository.save(currentOrder);
        return true;
    }

    public LocalDateTime getLastUpdatedTime(Long orderId) {
        return getOrderById(orderId).getOrderLastUpdatedAt();
    }

    public boolean setLastUpdatedTime(Long orderId, LocalDateTime lastUpdatedTime) {
        Order currentOrder = getOrderById(orderId);
        currentOrder.setOrderLastUpdatedAt(lastUpdatedTime);
        orderRepository.save(currentOrder);
        return true;
    }

    public String getDeliveryAddress(Long orderId) {
        return getOrderById(orderId).getDeliveryAddress();
    }

    public boolean setDeliveryAddress(Long orderId, String address) {
        Order currentOrder = getOrderById(orderId);
        currentOrder.setDeliveryAddress(address);
        orderRepository.save(currentOrder);
        return true;
    }

    public String getDeliveryMode(Long orderId) {
        return getOrderById(orderId).getDeliveryMode();
    }

    public boolean setDeliveryMode(Long orderId, String mode) {
        Order currentOrder = getOrderById(orderId);
        currentOrder.setDeliveryMode(mode);
        orderRepository.save(currentOrder);
        return true;
    }
}
