package com.back.global.security

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.entity.QMember.member
import lombok.Getter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails

data class SecurityUser(
    val member: Member
) : UserDetails {
    override fun getUsername(): String = member.getEmail()
    override fun getPassword(): String = member.getPassword()
    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf (SimpleGrantedAuthority("ROLE_USER"))
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}