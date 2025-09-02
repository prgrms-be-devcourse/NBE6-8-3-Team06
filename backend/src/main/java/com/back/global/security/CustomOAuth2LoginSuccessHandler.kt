package com.back.global.security

import com.back.domain.member.member.service.MemberService
import com.back.global.rq.Rq
import com.back.standard.extensions.base64Decode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class CustomOAuth2LoginSuccessHandler(
    private val memberService: MemberService,
    private val rq: Rq
): AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val actor = rq.actorFromDb

        val accessToken = memberService.geneAccessToken(actor)

        // 쿠키 설정
        rq.setCookie("accessToken", accessToken)

        // 프론트엔드로 리다이렉트 (3000 포트로 복원)
        val redirectUrl = request.getParameter("state")
            ?.let { encoded ->
                runCatching {
                    encoded.base64Decode()
                }.getOrNull()
            }
            ?.substringBefore('#')
            ?.takeIf { it.isNotBlank() }
            ?: "http://localhost:3000"

        rq.sendRedirect(redirectUrl)
    }
}
