package com.back.global.security;

import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.member.entity.Member;
import com.back.global.standard.util.Ut;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final MemberRepository memberRepository;

    @Value("${custom.jwt.secretKey}")
    private String secretKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractAccessTokenFromCookie(request);
        String path = request.getRequestURI();

        try {
            //토큰이 없거나 유효하지 않으면 401
            if(token == null||!Ut.jwt.isValid(secretKey,token)) {
                // 특정 API 경로는 토큰 없이 접근 허용
                if (path.startsWith("/books")){
                    filterChain.doFilter(request, response);
                    return;
                }
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"msg\": \"유효하지 않거나 누락된 accessToken입니다.\"}");
                return;
            }

            // 토큰이 유효한 경우
            Map<String,Object> payload = Ut.jwt.payload(secretKey,token);
            String email = (String) payload.get("email");

            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

            UserDetails userDetails = new SecurityUser(member);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);


        }catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"msg\": \"토큰 인증 과정에서 오류 발생\"}");
        }
    }

    private String extractAccessTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for(Cookie cookie : request.getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        return  path.equals("/user/signup") ||
                path.startsWith("/h2-console") ||
                path.equals("/user/reissue") ||
                path.equals("/user/login") ||
                path.startsWith("/swagger-ui") || path.startsWith("/v3")
                || path.startsWith("/categories");
    }
}
