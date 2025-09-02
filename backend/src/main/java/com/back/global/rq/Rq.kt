package com.back.global.rq

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.service.MemberService
import com.back.global.security.SecurityUser
import com.back.standard.extensions.getOrThrow
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component


@Component
class Rq(
    private val req: HttpServletRequest,
    private val resp: HttpServletResponse,
    private val memberService: MemberService
) {
    companion object {
        private const val ACCESS_TOKEN_DURATION = 60 * 20 // 20분
        private const val REFRESH_TOKEN_DURATION = 60 * 60 * 24 // 1일
        private const val DEFAULT_TOKEN_DURATION = 60 * 60 * 24 * 365 // 1년

        private const val LOCALHOST = "localhost"
        private const val PRODUCTION_DOMAIN = "bookers.p-e.kr"
    }

    val actor: Member?
        get() = SecurityContextHolder.getContext().authentication
            ?.principal
            ?.let { it as? SecurityUser }
            ?.member

    val actorFromDb: Member
            get() = memberService.findById(getAuthenticatedActor().id).getOrThrow()

    fun getAuthenticatedActor(): Member = actor
            ?: throw IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다")

    fun setCookie(name: String, value: String?) {
        val cookieValue = value ?: ""

        val domain = if (req.serverName.contains(LOCALHOST)) LOCALHOST else PRODUCTION_DOMAIN

        val cookie = Cookie(name, cookieValue).apply{
            path = "/"
            isHttpOnly = true
            secure = true
            setAttribute("SameSite", "None")
            setDomain(domain)
            maxAge = when {
                cookieValue.isEmpty() -> 0 // 즉시 만료
                name == "accessToken" -> ACCESS_TOKEN_DURATION
                name == "refreshToken" -> REFRESH_TOKEN_DURATION
                else -> DEFAULT_TOKEN_DURATION
            }
        }
        resp.addCookie(cookie)
    }

    fun clearAuthCookies() {
        listOf("accessToken", "refreshToken").forEach { name ->
            setCookie(name, "")
        }
    }

    fun sendRedirect(url: String) {
        resp.sendRedirect(url)
    }
}
