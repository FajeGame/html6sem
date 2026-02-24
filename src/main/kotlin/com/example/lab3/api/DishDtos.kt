package com.example.lab3.api

import com.example.lab3.domain.Dish
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

data class DishRequest(
    @field:NotBlank(message = "name is required")
    val name: String,
    @field:NotBlank(message = "description is required")
    val description: String,
    @field:DecimalMin(value = "0.0", inclusive = false, message = "price must be greater than 0")
    val price: BigDecimal,
    val isAvailable: Boolean? = true
)

data class DishResponse(
    val id: Long,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val isAvailable: Boolean
)

fun DishRequest.toDomain(id: Long = 0): Dish = Dish(
    id = id,
    name = name,
    description = description,
    price = price,
    isAvailable = isAvailable ?: true
)

fun Dish.toResponse(): DishResponse = DishResponse(
    id = id,
    name = name,
    description = description,
    price = price,
    isAvailable = isAvailable
)
