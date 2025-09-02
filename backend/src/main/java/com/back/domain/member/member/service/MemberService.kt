package com.back.domain.member.member.service

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.repository.MemberRepository
import com.back.global.exception.ServiceException
import com.back.global.rsData.RsData
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authTokenService: AuthTokenService
) {
    fun save(member: Member): Member {
        return memberRepository.save<Member>(member)
    }

    fun join(name: String, email: String, password: String): Member {
        val member = Member(name, email, password, null)
        return memberRepository.save<Member>(member)
    }

    fun join(name: String, email: String, password: String?, profileImgUrl: String?): Member {
        // 아이디 중복 체크: 존재하면 409 에러
        memberRepository.findByEmail(email)?.let {
            throw ServiceException("409-1", "이미 존재하는 아이디입니다.")
        }

        val encodedPassword = if (!password.isNullOrBlank()) passwordEncoder.encode(password) else null

        // 엔티티 생성 후 저장
        val member = Member(name, email, encodedPassword ?: "", profileImgUrl)
        return memberRepository.save(member)
    }

    // 문서 패턴대로 modifyOrJoin 추가 (email 기반으로!)
    fun modifyOrJoin(email: String, password: String?, name: String, profileImgUrl: String?): RsData<Member> =
        findByEmail(email)
            ?.let {
                // 기존 회원이면 정보 수정
                modify(it, name, profileImgUrl)
                RsData("200-1", "회원 정보가 수정되었습니다.", it)
            } ?: run {
            // 새 회원이면 가입
            val joined = join(name, email, password, profileImgUrl)
            RsData("201-1", "회원가입이 완료되었습니다.", joined)
        }

    fun modify(member: Member, name: String, profileImgUrl: String?) {
        member.profileImgUrl = profileImgUrl
    }

    fun findById(id: Int): Member? = memberRepository.findById(id).orElse(null)

    fun findByEmail(email: String): Member? {
        return memberRepository.findByEmail(email)
    }

    fun checkPassword(member: Member, password: String) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw ServiceException("401-1", "비밀번호가 일치하지 않습니다.")
        }
    }

    fun geneAccessToken(member: Member): String {
        return authTokenService.genAccessToken(member)
            ?: throw ServiceException("500", "액세스 토큰 생성에 실패했습니다.")
    }

    fun geneRefreshToken(member: Member): String {
        val refreshToken = authTokenService.genRefreshToken(member)
            ?: throw ServiceException("500", "리프레시 토큰 생성에 실패했습니다.")
        member.updateRefreshToken(refreshToken)
        return refreshToken
    }

    fun clearRefreshToken(member: Member) {
        member.clearRefreshToken()
    }

    fun isValidRefreshToken(refreshToken: String): Boolean {
        return authTokenService.isValid(refreshToken)
    }

    fun getRefreshTokenPayload(refreshToken: String): Map<String, Any>? {
        return authTokenService.payload(refreshToken)
    }

    fun payload(accessToken: String): Map<String, Any>? = authTokenService.payload(accessToken)

    fun deleteMember(member: Member) {
        memberRepository.delete(member)
    }
}
