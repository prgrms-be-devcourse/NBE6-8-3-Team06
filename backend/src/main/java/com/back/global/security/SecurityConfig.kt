package com.back.global.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val customOAuth2AuthorizationRequestResolver: CustomOAuth2AuthorizationRequestResolver,
    private val customOAuth2LoginSuccessHandler: CustomOAuth2LoginSuccessHandler
) {

    companion object {
        // 상수들을 compainion object로 정리
        private val PUBLIC_ENDPOINTS = arrayOf(
            "/user/login",
            "/user/signup",
            "/user/reissue",
            "/api/categories",
            "/oauth2/**", // OAuth2 관련 엔드포인트 추가
            "/login/oauth2/code/**" // OAuth2 콜백 엔드포인트 추가
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
            .oauth2Login { oauth2 ->
                oauth2
                    .authorizationEndpoint { authEndpoint ->
                        authEndpoint.authorizationRequestResolver(customOAuth2AuthorizationRequestResolver)
                    }
                    .userInfoEndpoint { userInfo ->
                        userInfo.userService(customOAuth2UserService)
                    }
                    .successHandler(customOAuth2LoginSuccessHandler)
                    .failureUrl("/oauth2/failure")
            }
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
