package com.back.global.jpa.entity

import jakarta.persistence.*
import lombok.AccessLevel
import lombok.Getter
import lombok.Setter
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
@Getter
abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PROTECTED)
    private val id = 0

    @CreatedDate
    private val createDate: LocalDateTime? = null

    @LastModifiedDate
    private val modifyDate: LocalDateTime? = null

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as BaseEntity
        return id == that.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }
}