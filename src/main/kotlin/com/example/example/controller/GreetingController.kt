package com.example.example.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/greeting")
class GreetingController {

    // Хранилище пользователей (в памяти)
    private val users = mutableMapOf<UUID, UserData>()

    // GET /greeting (с опциональным параметром id)
    @GetMapping
    fun getGreeting(@RequestParam(required = false) id: UUID?): Any {
        return if (id == null) {
            // Режим 1: без id - возвращаем простое приветствие
            GreetingMain("Hello World")
        } else {
            // Режим 2: с id - ищем пользователя
            users[id] ?: throw UserNotFoundException("User with id $id not found")
        }
    }

    // POST /greeting - создание пользователя
    @PostMapping
    fun createUser(@RequestBody userData: UserData): GreetingUser {
        val id = UUID.randomUUID()
        users[id] = userData
        return GreetingUser(
            text = "Hello, ${userData.surname} ${userData.name}",
            id = id
        )
    }

    // GET /greeting/{id} - получение пользователя по path
    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: UUID): UserData {
        return users[id] ?: throw UserNotFoundException("User with id $id not found")
    }

    // Обработка исключений
    @ResponseStatus(HttpStatus.NOT_FOUND)
    class UserNotFoundException(message: String) : RuntimeException(message)
}