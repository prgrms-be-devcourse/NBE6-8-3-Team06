package com.back.domain.member.member.controller

import com.back.domain.member.member.dto.MemberDto
import com.back.domain.member.member.dto.MemberJoinReqDto
import com.back.domain.member.member.dto.MemberLoginReqDto
import com.back.domain.member.member.dto.MemberLoginResDto
import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.service.MemberService
import com.back.global.exception.ServiceException
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import lombok.RequiredArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.util.function.Consumer
import java.util.function.Supplier

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
class MemberController {
    private val memberService: MemberService? = null
    private val passwordEncoder: PasswordEncoder? = null
    private val rq: Rq? = null

    @PostMapping("/signup")
    @Transactional
    fun join(
        @RequestBody reqBody: @Valid MemberJoinReqDto
    ): RsData<MemberDto?> {
        memberService!!.findByEmail(reqBody.email)!!
            .ifPresent(Consumer { member: Member? ->
                throw ServiceException("409", "이미 존재하는 이메일 입니다. 다시 입력해주세요.")
            })
        val member = memberService.join(
            reqBody.name!!,
            reqBody.email!!,
            passwordEncoder!!.encode(reqBody.password)
        )
        return RsData<MemberDto?>(
            "201-1",
            "${member.getName()}님 환영합니다. Bookers 회원가입이 완료되었습니다.",
            MemberDto(member)
        )
    }

    @PostMapping("/login")
    @Transactional
    fun login(
        @RequestBody reqBody: @Valid MemberLoginReqDto
    ): RsData<MemberLoginResDto?> {
        val member = memberService!!.findByEmail(reqBody.email)!!
            .orElseThrow<ServiceException?>(Supplier { ServiceException("401-1", "존재하지 않는 아이디입니다.") })

        memberService.checkPassword(member!!, reqBody.password)

        val accessToken = memberService.geneAccessToken(member)
        val refreshToken = memberService.geneRefreshToken(member)

        member.updateRefreshToken(refreshToken)
        memberService.save(member)

        rq!!.setCookie("accessToken", accessToken)
        rq.setCookie("refreshToken", refreshToken)

        return RsData<MemberLoginResDto?>(
            "200-1",
            "${member.getEmail()}님 환영합니다." ,
            MemberLoginResDto(
                MemberDto(member),
                accessToken
            )
        )
    }

    @PostMapping("/logout")
    fun logout(): ResponseEntity<*> {
        val actor = rq!!.getActor()

        if (actor != null) {
            // 서버에서 refresh 토큰 삭제
            memberService!!.clearRefreshToken(actor)
        }

        // 쿠키에서 토큰 삭제
        rq.clearAuthCookies()

        return ResponseEntity.noContent().build<Any?>()
    }

    @get:GetMapping("/my")
    val authenticatedUser: ResponseEntity<*>
        get() {
            val actor = rq!!.getActor()

            if (actor == null) {
                return ResponseEntity.status(401).body<String?>("로그인 상태가 아닙니다.") // 인증되지 않은 사용자에 대한 처리
            }

            return ResponseEntity.ok<MemberDto?>(MemberDto(actor))
        }

    @PostMapping("/reissue")
    @Transactional
    fun reissue(request: HttpServletRequest): RsData<*> {
        var refreshToken: String? = null

        if (request.getCookies() != null) {
            for (cookie in request.getCookies()) {
                if (cookie.getName() == "refreshToken") {
                    refreshToken = cookie.getValue()
                    break
                }
            }
        }

        if (refreshToken == null || !memberService!!.isValidRefreshToken(refreshToken)) {
            return RsData<Any?>("400", "유효하지 않은 RefreshToken 입니다.", null)
        }

        val payload = memberService.getRefreshTokenPayload(refreshToken)
        val email: String? = payload!!.get("email").toString()

        val member = memberService.findByEmail(email)!!
            .orElseThrow<ServiceException?>(Supplier { ServiceException("401-1", "사용자를 찾을 수 없습니다.") })
        if (refreshToken != member!!.getRefreshToken()) {
            return RsData<Any?>("401", "서버에 저장된 토큰과 일치하지 않습니다.", null)
        }
        val newAccessToken = memberService.geneAccessToken(member)
        rq!!.setCookie("accessToken", newAccessToken)

        return RsData<Any?>("200", "AccessToken이 재발급되었습니다.", null)
    }

    @DeleteMapping("/my")
    fun deleteMember(response: HttpServletResponse?): RsData<String?> {
        val actor = rq!!.getActor()
        if (actor == null) {
            return RsData<String?>("401-1", "로그인 상태가 아닙니다.", null)
        }

        // 회원 삭제
        memberService!!.deleteMember(actor)

        // 쿠키에서 토큰 삭제
        rq.clearAuthCookies()

        return RsData<String?>("200-1", "회원탈퇴가 완료되었습니다.", null)
    }
}