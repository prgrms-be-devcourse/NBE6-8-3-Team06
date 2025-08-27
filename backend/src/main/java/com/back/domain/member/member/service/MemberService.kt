package com.back.domain.member.member.service

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.repository.MemberRepository
import com.back.global.exception.ServiceException
import lombok.RequiredArgsConstructor
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
@RequiredArgsConstructor
class MemberService {
    private val memberRepository: MemberRepository? = null
    private val passwordEncoder: PasswordEncoder? = null
    private val authTokenService: AuthTokenService? = null

    fun save(member: Member): Member {
        return memberRepository!!.save<Member>(member)
    }

    fun join(name: String, email: String, password: String): Member {
        val member = Member(name, email, password)
        return memberRepository!!.save<Member>(member)
    }

    fun findByEmail(email: String?): Optional<Member?>? {
        return memberRepository!!.findByEmail(email)
    }

    fun checkPassword(member: Member, password: String?) {
        if (!passwordEncoder!!.matches(password, member.getPassword())) {
            throw ServiceException("401-1", "비밀번호가 일치하지 않습니다.")
        }
    }

    fun geneAccessToken(member: Member): String? {
        return authTokenService!!.genAccessToken(member)
    }

    fun geneRefreshToken(member: Member): String? {
        val refreshToken = authTokenService!!.genRefreshToken(member)
        member.updateRefreshToken(refreshToken)
        return refreshToken
    }

    fun clearRefreshToken(member: Member) {
        member.clearRefreshToken()
    }

    fun isValidRefreshToken(refreshToken: String?): Boolean {
        return authTokenService!!.isValid(refreshToken)
    }

    fun getRefreshTokenPayload(refreshToken: String?): MutableMap<String?, Any?>? {
        return authTokenService!!.payload(refreshToken)
    }

    fun deleteMember(member: Member) {
        memberRepository!!.delete(member)
    }
}
