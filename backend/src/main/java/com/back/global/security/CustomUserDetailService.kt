package com.back.global.security

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.service.MemberService
import lombok.RequiredArgsConstructor
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
@RequiredArgsConstructor
class CustomUserDetailService : UserDetailsService {
    private val memberService: MemberService? = null

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(email: String): UserDetails {
        val member: Member = memberService!!.findByEmail(email)
            .orElseThrow({ UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email) })

        return SecurityUser(member)
    }
}
