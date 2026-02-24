package com.example.lab3.api

import com.example.lab3.application.DishService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/dishes")
class DishController(
    private val dishService: DishService
) {
    @PostMapping
    fun create(@Valid @RequestBody body: DishRequest): ResponseEntity<DishResponse> {
        val (result, created) = dishService.createOrGet(body.toDomain())
        val status = if (created) HttpStatus.CREATED else HttpStatus.OK
        return ResponseEntity.status(status).body(result.toResponse())
    }

    @GetMapping
    fun findAll(@RequestParam(name = "namePart", required = false) namePart: String?): List<DishResponse> {
        return dishService.findAll(namePart).map { it.toResponse() }
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<DishResponse> {
        val dish = dishService.findById(id) ?: throw NotFoundException("Dish with id=$id not found")
        return ResponseEntity.ok(dish.toResponse())
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody body: DishRequest): ResponseEntity<DishResponse> {
        val dish = dishService.update(id, body.toDomain()) ?: throw NotFoundException("Dish with id=$id not found")
        return ResponseEntity.ok(dish.toResponse())
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        if (!dishService.deleteById(id)) throw NotFoundException("Dish with id=$id not found")
        return ResponseEntity.noContent().build()
    }
}
