package com.example.lab3.domain

interface DishRepositoryPort {
    fun create(dish: Dish): Dish
    fun update(dish: Dish): Dish?
    fun findById(id: Long): Dish?
    fun findByName(name: String): Dish?
    fun findAll(namePart: String?): List<Dish>
    fun deleteById(id: Long): Boolean
}
