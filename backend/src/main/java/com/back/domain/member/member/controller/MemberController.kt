package com.back.domain.member.member.controller

import com.back.domain.member.member.dto.MemberDto
import com.back.domain.member.member.dto.MemberJoinReqDto
import com.back.domain.member.member.dto.MemberLoginReqDto
import com.back.domain.member.member.dto.MemberLoginResDto
import com.back.domain.member.member.service.MemberService
import com.back.global.exception.ServiceException
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class MemberController(
    private val memberService: MemberService,
    private val passwordEncoder: PasswordEncoder,
    private val rq: Rq
) {
    @PostMapping("/signup")
    @Transactional
    fun join(
        @RequestBody @Valid reqBody: MemberJoinReqDto
    ): RsData<MemberDto> {
        memberService.findByEmail(reqBody.email)?.let {
                throw ServiceException("409", "이미 존재하는 이메일 입니다. 다시 입력해주세요.")
        }

        val member = memberService.join(
            reqBody.name,
            reqBody.email,
            passwordEncoder.encode(reqBody.password)
        )
        return RsData(
            "201-1",
            "${member.getName()}님 환영합니다. Bookers 회원가입이 완료되었습니다.",
            MemberDto(member)
        )
    }

    @PostMapping("/login")
    @Transactional
    fun login(
        @RequestBody @Valid reqBody: MemberLoginReqDto
    ): RsData<MemberLoginResDto> {
        val member = memberService.findByEmail(reqBody.email)
            ?: throw ServiceException("401-1", "존재하지 않는 아이디입니다.")

        memberService.checkPassword(member, reqBody.password)

        val accessToken = memberService.geneAccessToken(member)
        val refreshToken = memberService.geneRefreshToken(member)

        member.updateRefreshToken(refreshToken)
        memberService.save(member)

        rq.setCookie("accessToken", accessToken)
        rq.setCookie("refreshToken", refreshToken)

        return RsData(
            "200-1",
            "${member.getEmail()}님 환영합니다." ,
            MemberLoginResDto(
                MemberDto(member),
                accessToken
            )
        )
    }

    @PostMapping("/logout")
    fun logout(): ResponseEntity<Void> {
        val actor = rq.getActor()?.let { actor ->
            memberService.clearRefreshToken(actor)
        }
        // 쿠키에서 토큰 삭제
        rq.clearAuthCookies()

        return ResponseEntity.noContent().build()
    }

    @GetMapping("/my")
    fun getAuthenticatedUser(): ResponseEntity<*> {
            val actor = rq.getActor()
                ?: return ResponseEntity.status(401).body("로그인 상태가 아닙니다.")

            return ResponseEntity.ok(MemberDto(actor))
    }

    @PostMapping("/reissue")
    @Transactional
    fun reissue(request: HttpServletRequest): RsData<Any> {
        val refreshToken = request.cookies
            ?.find { it.name == "refresh_token" }
            ?.value
            ?: return RsData("400", "RefreshToken이 존재하지 않습니다.", null)

        if (!memberService.isValidRefreshToken(refreshToken)) {
            return RsData("400", "유효하지 않은 RefreshToken 입니다.", null)
        }

        val payload = memberService.getRefreshTokenPayload(refreshToken)
            ?: return RsData("400", "RefreshToken의 내용을 가져올 수 없습니다.", null)

        val email = payload["email"] as? String
            ?: return RsData("400", "잘못된 토큰 형식입니다.", null)

        val member = memberService.findByEmail(email)
            ?: throw ServiceException("401-1", "사용자를 찾을 수 없습니다.")

        if (refreshToken != member.getRefreshToken()) {
            return RsData("401", "서버에 저장된 토큰과 일치하지 않습니다.", null)
        }

        val newAccessToken = memberService.geneAccessToken(member)
        rq.setCookie("accessToken", newAccessToken)

        return RsData("200", "AccessToken이 재발급되었습니다.", null)
    }

    @DeleteMapping("/my")
    fun deleteMember(): RsData<String?> {
        val actor = rq.getActor()
            ?: return RsData("401-1", "로그인 상태가 아닙니다.", null)

        // 회원 삭제
        memberService.deleteMember(actor)

        // 쿠키에서 토큰 삭제
        rq.clearAuthCookies()

        return RsData("200-1", "회원탈퇴가 완료되었습니다.", null)
    }
}