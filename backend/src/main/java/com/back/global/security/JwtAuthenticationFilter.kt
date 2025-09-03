package com.back.global.security

import com.back.domain.member.member.repository.MemberRepository
import com.back.global.standard.util.Ut
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtAuthenticationFilter (
    private val memberRepository: MemberRepository,
    @Value("\${custom.jwt.secretKey}")
    private val secretKey: String,
    private val authenticationEventPublisher: DefaultAuthenticationEventPublisher
): OncePerRequestFilter() {

    companion object {
        private const val ACCESS_TOKEN_COOKIE = "accessToken"

        // 토큰 없이 접근 허용
        private val PUBLIC_PATHS = setOf(
            "/user/signup",
            "/user/login",
            "/user/reissue"
        )

        private val PUBLIC_PATH_PREFIXES = setOf(
            "/h2-console",
            "/swagger-ui",
            "/v3",
            "/categories"
        )
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = extractAccessTokenFromCookie(request)
        val path = request.requestURI

        try {
            //토큰이 없거나 유효하지 않은 경우
            if (token == null || !Ut.jwt.isValid(secretKey, token)) {
                // 특정 API 경로는 토큰 없이 접근 허용
                if (isPublicPath(path)) {
                    filterChain.doFilter(request, response)
                    return
                }
                if (path.startsWith("/book")){
                    filterChain.doFilter(request, response)
                    return
                }

                sendUnauthorizedResponse(response, "유효하지 않거나 누락된 accessToken입니다.")
                return
            }

            // 토큰이 유효한 경우
            authenticateUser(token)
            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
            response.setContentType("application/json")
            response.setCharacterEncoding("UTF-8")
            response.getWriter().write("{\"msg\": \"토큰 인증 과정에서 오류 발생\"}")
        }
    }

    private fun authenticateUser(token: String) {
        val payload = Ut.jwt.payload(secretKey, token)
            ?: throw RuntimeException("토큰의 payload를 읽을 수 없습니다.")

        val email = payload["email"] as? String
            ?: throw RuntimeException("email 정보가 없습니다.")

        val member = memberRepository.findByEmail(email)
            ?: throw RuntimeException("사용자 정보를 찾을 수 없습니다.")

        val userDetails = SecurityUser(member)
        val authentication = UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities
        )

        SecurityContextHolder.getContext().authentication = authentication
    }

    private fun sendUnauthorizedResponse(response: HttpServletResponse, message: String) {
        response.apply{
            status = HttpServletResponse.SC_UNAUTHORIZED
            contentType = "application/json"
            characterEncoding = "UTF-8"
            writer.write("{\"msg\": \"$message\"}")
        }
    }

    private fun isPublicPath(path: String): Boolean {
        return path in PUBLIC_PATHS || PUBLIC_PATH_PREFIXES.any { path.startsWith(it) }
    }

    private fun extractAccessTokenFromCookie(request: HttpServletRequest): String? {
        return request.cookies
            ?.find { it.name == ACCESS_TOKEN_COOKIE }
            ?.value
    }


    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return isPublicPath(request.requestURI)
    }
}
