package com.example.lab3.application

import com.example.lab3.domain.Dish
import com.example.lab3.domain.DishRepositoryPort
import org.springframework.stereotype.Service

@Service
class DishService(
    private val dishRepositoryPort: DishRepositoryPort
) {
    fun createOrGet(dish: Dish): Pair<Dish, Boolean> {
        val existing = dishRepositoryPort.findByName(dish.name)
        if (existing != null) return existing to false
        return dishRepositoryPort.create(dish) to true
    }

    fun update(id: Long, dish: Dish): Dish? = dishRepositoryPort.update(dish.copy(id = id))

    fun findById(id: Long): Dish? = dishRepositoryPort.findById(id)

    fun findAll(namePart: String?): List<Dish> = dishRepositoryPort.findAll(namePart)

    fun deleteById(id: Long): Boolean = dishRepositoryPort.deleteById(id)
}
