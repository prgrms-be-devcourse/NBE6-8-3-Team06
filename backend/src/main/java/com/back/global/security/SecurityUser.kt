package com.back.global.security

import com.back.domain.member.member.entity.Member
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User

data class SecurityUser(
    val member: Member,
    private val attributes: Map<String, Any> = emptyMap()
) : UserDetails, OAuth2User {
    
    // UserDetails 구현 (기존 방식 유지)
    override fun getUsername(): String = member.getEmail()
    override fun getPassword(): String = member.getPassword()
    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_USER"))
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
    
    // OAuth2User 구현
    override fun getAttributes(): Map<String, Any> = attributes
    override fun getName(): String = member.id.toString()
    
    companion object {
        // 일반 로그인용 (기존)
        fun create(member: Member): SecurityUser {
            return SecurityUser(member)
        }
        
        // OAuth2 로그인용 (새로 추가)
        fun create(member: Member, attributes: Map<String, Any>): SecurityUser {
            return SecurityUser(member, attributes)
        }
    }
}
