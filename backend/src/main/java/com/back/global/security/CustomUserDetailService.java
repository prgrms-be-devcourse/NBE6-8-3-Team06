package com.back.global.security;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final MemberService memberService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberService.findByEmail(email);
        if (member == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
        }

        return new SecurityUser(member);
    }
}
