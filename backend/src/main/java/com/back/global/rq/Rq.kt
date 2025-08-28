package com.back.global.rq

import com.back.domain.member.member.entity.Member
import com.back.global.security.SecurityUser
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.*
import java.util.function.Function
import java.util.function.Predicate

@Component
@RequiredArgsConstructor
class Rq {
    private val req: HttpServletRequest? = null
    private val resp: HttpServletResponse? = null

    val actor: Member?
        get() = Optional.ofNullable<Authentication?>(
            SecurityContextHolder.getContext().getAuthentication()
        )
            .map<Any?>(Function { obj: Authentication? -> obj!!.getPrincipal() })
            .filter(Predicate { obj: Any? -> SecurityUser::class.java.isInstance(obj) })
            .map<SecurityUser?>(Function { obj: Any? -> SecurityUser::class.java.cast(obj) })
            .map<Member?>(SecurityUser::member)
            .orElse(null)

    fun setCookie(name: String, value: String?) {
        var value = value
        if (value == null) value = ""

        val domain = if (req!!.getServerName().contains("localhost")) "localhost" else "bookers.p-e.kr"

        val cookie = Cookie(name, value)
        cookie.setPath("/")
        cookie.setHttpOnly(true)
        cookie.setSecure(true)
        cookie.setAttribute("SameSite", "None")
        cookie.setDomain(domain)

        if (value.isEmpty()) {
            cookie.setMaxAge(0) // 즉시 만료
        } else {
            if (name == "accessToken") {
                cookie.setMaxAge(60 * 20) //20분
            } else if (name == "refreshToken") {
                cookie.setMaxAge(60 * 60 * 24) // 1일
            } else {
                cookie.setMaxAge(60 * 60 * 24 * 365) // 1년
            }
        }
        resp!!.addCookie(cookie)
    }

    fun clearAuthCookies() {
        setCookie("accessToken", "")
        setCookie("refreshToken", "")
    }
}
