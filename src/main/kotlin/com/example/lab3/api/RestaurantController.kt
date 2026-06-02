package com.example.lab3.api

import com.example.lab3.application.DishService
import com.example.lab3.application.RestaurantService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/restaurants")
class RestaurantController(
    private val restaurantService: RestaurantService,
    private val dishService: DishService
) {
    @PostMapping
    fun create(@Valid @RequestBody body: RestaurantRequest): ResponseEntity<RestaurantResponse> {
        val result = restaurantService.create(body.toDomain())
        return ResponseEntity.status(HttpStatus.CREATED).body(result.toResponse())
    }

    @GetMapping
    fun findAll(): List<RestaurantResponse> = restaurantService.findAll().map { it.toResponse() }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<RestaurantResponse> {
        val restaurant = restaurantService.findById(id) ?: throw NotFoundException("Restaurant with id=$id not found")
        return ResponseEntity.ok(restaurant.toResponse())
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody body: RestaurantRequest): ResponseEntity<RestaurantResponse> {
        val restaurant = restaurantService.update(id, body.toDomain())
            ?: throw NotFoundException("Restaurant with id=$id not found")
        return ResponseEntity.ok(restaurant.toResponse())
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        if (!restaurantService.deleteById(id)) throw NotFoundException("Restaurant with id=$id not found")
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/dishes")
    fun getMenu(@PathVariable id: Long): List<DishResponse> {
        restaurantService.findById(id) ?: throw NotFoundException("Restaurant with id=$id not found")
        return dishService.findByRestaurantId(id).map { it.toResponse() }
    }

    @PostMapping("/{restaurantId}/dishes")
    fun createDish(
        @PathVariable restaurantId: Long,
        @Valid @RequestBody body: DishRequest
    ): ResponseEntity<DishResponse> {
        restaurantService.findById(restaurantId) ?: throw NotFoundException("Restaurant with id=$restaurantId not found")
        val result = dishService.create(body.toDomain(restaurantId = restaurantId))
        return ResponseEntity.status(HttpStatus.CREATED).body(result.toResponse())
    }
}
