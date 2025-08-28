package com.back.global.security

import com.back.domain.member.member.entity.Member
import lombok.Getter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

@Getter
class SecurityUser(private val member: Member) : User(
    member.getEmail(), member.getPassword(), mutableListOf<GrantedAuthority?>()
) 