package com.back.global.security

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.repository.MemberRepository
import com.back.global.standard.util.Ut
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
@RequiredArgsConstructor
class JwtAuthenticationFilter : OncePerRequestFilter() {
    private val memberRepository: MemberRepository? = null

    @Value("\${custom.jwt.secretKey}")
    private val secretKey: String? = null

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = extractAccessTokenFromCookie(request)
        val path = request.getRequestURI()

        try {
            //토큰이 없거나 유효하지 않으면 401
            if (token == null || !Ut.jwt.isValid(secretKey!!, token)) {
                // 특정 API 경로는 토큰 없이 접근 허용
                if (path.startsWith("/books")) {
                    filterChain.doFilter(request, response)
                    return
                }
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
                response.setContentType("application/json")
                response.setCharacterEncoding("UTF-8")
                response.getWriter().write("{\"msg\": \"유효하지 않거나 누락된 accessToken입니다.\"}")
                return
            }

            // 토큰이 유효한 경우
            val payload = Ut.jwt.payload(secretKey, token)
            val email = payload!!.get("email") as String

            val member: Member = memberRepository!!.findByEmail(email)
                .orElseThrow({ RuntimeException("사용자 정보를 찾을 수 없습니다.") })

            val userDetails: UserDetails = SecurityUser(member)
            val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())

            SecurityContextHolder.getContext().setAuthentication(authentication)
            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
            response.setContentType("application/json")
            response.setCharacterEncoding("UTF-8")
            response.getWriter().write("{\"msg\": \"토큰 인증 과정에서 오류 발생\"}")
        }
    }

    private fun extractAccessTokenFromCookie(request: HttpServletRequest): String? {
        if (request.getCookies() == null) return null

        for (cookie in request.getCookies()) {
            if ("accessToken" == cookie.getName()) {
                return cookie.getValue()
            }
        }
        return null
    }

    @Throws(ServletException::class)
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.getRequestURI()

        return path == "/user/signup" ||
                path.startsWith("/h2-console") ||
                path == "/user/reissue" ||
                path == "/user/login" ||
                path.startsWith("/swagger-ui") || path.startsWith("/v3")
                || path.startsWith("/categories")
    }
}
