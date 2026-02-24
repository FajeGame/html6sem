package com.example.lab3.infrastructure.jpa

import com.example.lab3.domain.Dish
import com.example.lab3.domain.DishRepositoryPort

class DishJpaAdapter(
    private val dishJpaRepository: DishJpaRepository
) : DishRepositoryPort {
    override fun create(dish: Dish): Dish = dishJpaRepository.save(DishEntity.fromDomain(dish)).toDomain()

    override fun update(dish: Dish): Dish? {
        if (!dishJpaRepository.existsById(dish.id)) return null
        return dishJpaRepository.save(DishEntity.fromDomain(dish)).toDomain()
    }

    override fun findById(id: Long): Dish? = dishJpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByName(name: String): Dish? = dishJpaRepository.findByName(name)?.toDomain()

    override fun findAll(namePart: String?): List<Dish> {
        val entities = if (namePart.isNullOrBlank()) {
            dishJpaRepository.findAll()
        } else {
            dishJpaRepository.findByNamePart(namePart)
        }
        return entities.map { it.toDomain() }
    }

    override fun deleteById(id: Long): Boolean {
        if (!dishJpaRepository.existsById(id)) return false
        dishJpaRepository.deleteById(id)
        return true
    }
}
