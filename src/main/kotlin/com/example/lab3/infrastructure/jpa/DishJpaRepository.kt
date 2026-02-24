package com.example.lab3.infrastructure.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface DishJpaRepository : JpaRepository<DishEntity, Long> {
    fun findByName(name: String): DishEntity?

    @Query(
        """
        select d
        from DishEntity d
        where lower(d.name) like lower(concat('%', :namePart, '%'))
        order by d.id
        """
    )
    fun findByNamePart(namePart: String): List<DishEntity>
}
