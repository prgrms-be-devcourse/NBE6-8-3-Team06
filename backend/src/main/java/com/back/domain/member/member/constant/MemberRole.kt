package com.back.domain.member.member.constant

enum class MemberRole(val value: String) {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");
    
    companion object {
        fun fromValue(value: String): MemberRole? {
            return values().find { it.value == value }
        }
    }
}
