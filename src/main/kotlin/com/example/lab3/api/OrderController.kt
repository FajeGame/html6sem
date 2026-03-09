package com.example.lab3.api

import com.example.lab3.application.OrderService
import com.example.lab3.domain.OrderStatus
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderService: OrderService
) {
    @PostMapping
    fun create(@Valid @RequestBody body: OrderCreateRequest): ResponseEntity<OrderResponse> {
        val order = orderService.create(
            userId = body.userId ?: throw IllegalArgumentException("userId is required"),
            dishIds = body.dishIds ?: throw IllegalArgumentException("dishIds must not be empty")
        )
        val dishes = orderService.getDishes(order)
        return ResponseEntity.status(HttpStatus.CREATED).body(order.toResponse(dishes))
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<OrderResponse> {
        val order = orderService.findById(id) ?: throw NotFoundException("Order with id=$id not found")
        val dishes = orderService.getDishes(order)
        return ResponseEntity.ok(order.toResponse(dishes))
    }

    @GetMapping
    fun findAll(
        @RequestParam(required = false) userId: Long?,
        @RequestParam(required = false) status: OrderStatus?
    ): List<OrderResponse> {
        return orderService.findAll(userId, status).map { order ->
            order.toResponse(orderService.getDishes(order))
        }
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(@PathVariable id: Long, @Valid @RequestBody body: OrderStatusUpdateRequest): ResponseEntity<OrderResponse> {
        if (orderService.findById(id) == null) throw NotFoundException("Order with id=$id not found")
        val updated = orderService.updateStatus(id, body.status ?: throw IllegalArgumentException("status is required"))
        val dishes = orderService.getDishes(updated)
        return ResponseEntity.ok(updated.toResponse(dishes))
    }
}
