package com.example.lab3.application

import com.example.lab3.domain.Restaurant
import com.example.lab3.domain.RestaurantRepositoryPort
import org.springframework.stereotype.Service

@Service
class RestaurantService(
    private val restaurantRepositoryPort: RestaurantRepositoryPort
) {
    fun create(restaurant: Restaurant): Restaurant = restaurantRepositoryPort.create(restaurant)

    fun update(id: Long, restaurant: Restaurant): Restaurant? =
        restaurantRepositoryPort.update(restaurant.copy(id = id))

    fun findById(id: Long): Restaurant? = restaurantRepositoryPort.findById(id)

    fun findAll(): List<Restaurant> = restaurantRepositoryPort.findAll()

    fun deleteById(id: Long): Boolean = restaurantRepositoryPort.deleteById(id)
}
