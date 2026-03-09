package com.example.lab3.application

import com.example.lab3.domain.Order
import com.example.lab3.domain.OrderRepositoryPort
import com.example.lab3.domain.OrderStatus
import com.example.lab3.domain.Dish
import com.example.lab3.domain.UserRepositoryPort
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrderService(
    private val orderRepositoryPort: OrderRepositoryPort,
    private val userRepositoryPort: UserRepositoryPort,
    private val dishService: DishService
) {
    fun create(userId: Long, dishIds: List<Long>): Order {
        if (dishIds.isEmpty()) throw IllegalArgumentException("dishIds must not be empty")
        if (userRepositoryPort.findById(userId) == null) throw IllegalArgumentException("User with id=$userId not found")
        val dishes = dishService.findByIds(dishIds)
        if (dishes.size != dishIds.distinct().size) throw IllegalArgumentException("Some dishes not found")
        return orderRepositoryPort.create(
            Order(
                id = 0,
                userId = userId,
                status = OrderStatus.PENDING,
                createdAt = LocalDateTime.now(),
                dishIds = dishIds.distinct()
            )
        )
    }

    fun findById(id: Long): Order? = orderRepositoryPort.findById(id)

    fun findAll(userId: Long?, status: OrderStatus?): List<Order> = orderRepositoryPort.findAll(userId, status)

    fun updateStatus(id: Long, newStatus: OrderStatus): Order {
        val existing = orderRepositoryPort.findById(id) ?: throw IllegalArgumentException("Order with id=$id not found")
        if (!isTransitionAllowed(existing.status, newStatus)) throw IllegalArgumentException("Invalid status transition")
        return orderRepositoryPort.update(existing.copy(status = newStatus)) ?: existing
    }

    fun getDishes(order: Order): List<Dish> = dishService.findByIds(order.dishIds)

    private fun isTransitionAllowed(from: OrderStatus, to: OrderStatus): Boolean {
        if (from == to) return true
        return when (from) {
            OrderStatus.PENDING -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED
            OrderStatus.CONFIRMED -> to == OrderStatus.DELIVERED || to == OrderStatus.CANCELLED
            OrderStatus.DELIVERED -> false
            OrderStatus.CANCELLED -> false
        }
    }
}
