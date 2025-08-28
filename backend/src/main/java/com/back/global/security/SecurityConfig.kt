package com.back.global.security

import lombok.RequiredArgsConstructor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.*
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@RequiredArgsConstructor
class SecurityConfig {
    private val jwtAuthenticationFilter: JwtAuthenticationFilter? = null

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain? {
        http
            .csrf(Customizer { csrf: CsrfConfigurer<HttpSecurity?>? -> csrf!!.disable() })
            .cors(Customizer.withDefaults<CorsConfigurer<HttpSecurity?>?>())
            .headers(Customizer { headers: HeadersConfigurer<HttpSecurity?>? -> headers!!.frameOptions(Customizer { frame: FrameOptionsConfig? -> frame.sameOrigin() }) })

            .authorizeHttpRequests(Customizer { auth: AuthorizationManagerRequestMatcherRegistry? ->
                auth
                    .requestMatchers("/user/login", "/user/signup", "/user/reissue").permitAll()
                    .requestMatchers("/api/categories").permitAll()
                    .requestMatchers("/user/my").authenticated()
                    .anyRequest().permitAll()
            }
            ) // jwt 인증필터 등록

            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

            .httpBasic(Customizer { httpBasic: HttpBasicConfigurer<HttpSecurity?>? -> httpBasic!!.disable() })
            .formLogin(Customizer { formLogin: FormLoginConfigurer<HttpSecurity?>? -> formLogin!!.disable() })
        http.logout(Customizer { logout: LogoutConfigurer<HttpSecurity?>? -> logout!!.disable() })

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.setAllowedOrigins(
            mutableListOf<String?>(
                "http://localhost:3000",
                "https://www.bookers.p-e.kr",
                "https://bookers.p-e.kr"
            )
        ) // 프론트 도메인
        config.setAllowedMethods(mutableListOf<String?>("GET", "POST", "PUT", "DELETE", "PATCH"))
        config.setAllowedHeaders(mutableListOf<String?>("*"))
        config.setAllowCredentials(true)

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config) // 모든 경로에 적용

        return source
    }
}
