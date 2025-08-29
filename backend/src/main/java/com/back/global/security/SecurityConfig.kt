package com.back.global.security

import lombok.RequiredArgsConstructor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.*
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    companion object {
        // 상수들을 compainion object로 정리
        private val PUBLIC_ENDPOINTS = arrayOf(
            "/user/login",
            "/user/signup",
            "/user/reissue",
            "/api/categories"
        )

        private val ALLOWED_ORIGINS = listOf(
            "http://localhost:3000",
            "https://www.bookers.p-e.kr",
            "https://bookers.p-e.kr"
        )

        private val ALLOWED_METHODS = listOf("GET", "POST", "PUT", "DELETE", "PATCH")
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .headers { headers ->
                headers.frameOptions { it.sameOrigin() }
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests{ auth ->
                auth
                    .requestMatchers(*PUBLIC_ENDPOINTS).permitAll()
                    .requestMatchers("/user/my").authenticated()
                    .anyRequest().permitAll()
            }
             // jwt 인증필터 등록
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )
            .httpBasic{ it.disable() }
            .formLogin{ it.disable() }
            .logout { it.disable() }
            .build()

    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = ALLOWED_ORIGINS
            allowedMethods = ALLOWED_METHODS
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}
