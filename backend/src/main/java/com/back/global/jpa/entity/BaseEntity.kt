package com.back.global.jpa.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0
    protected set

    @CreatedDate
    var createDate: LocalDateTime = LocalDateTime.now()
    protected set

    @LastModifiedDate
    var modifyDate: LocalDateTime = LocalDateTime.now()
    protected set

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other == null || this::class != other::class) return false
        other as BaseEntity
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}