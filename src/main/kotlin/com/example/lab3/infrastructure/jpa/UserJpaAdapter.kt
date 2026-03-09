package com.example.lab3.infrastructure.jpa

import com.example.lab3.domain.User
import com.example.lab3.domain.UserRepositoryPort

class UserJpaAdapter(
    private val userJpaRepository: UserJpaRepository
) : UserRepositoryPort {
    override fun create(user: User): User = userJpaRepository.save(UserEntity.fromDomain(user)).toDomain()

    override fun update(user: User): User? {
        if (!userJpaRepository.existsById(user.id)) return null
        return userJpaRepository.save(UserEntity.fromDomain(user)).toDomain()
    }

    override fun findById(id: Long): User? = userJpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByEmail(email: String): User? = userJpaRepository.findByEmail(email)?.toDomain()

    override fun findAll(): List<User> = userJpaRepository.findAll().map { it.toDomain() }

    override fun deleteById(id: Long): Boolean {
        if (!userJpaRepository.existsById(id)) return false
        userJpaRepository.deleteById(id)
        return true
    }
}
