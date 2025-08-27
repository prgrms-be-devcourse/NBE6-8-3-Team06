package com.back.domain.member.member.service;

import com.back.domain.bookmarks.repository.BookmarkRepository;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.member.entity.Member;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    public Member save (Member member) { return memberRepository.save(member); }
    
    public Member join(String name, String email, String password){
        Member member = new Member(name, email, password);
        return memberRepository.save(member);
    }
    
    public Optional<Member> findByEmail(String email){ 
        return memberRepository.findByEmail(email); 
    }

    public void checkPassword(Member member, String password) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new ServiceException("401-1","비밀번호가 일치하지 않습니다.");
        }
    }
    public String geneAccessToken(Member member){ return authTokenService.genAccessToken(member);}
    public String geneRefreshToken(Member member){
        String refreshToken = authTokenService.genRefreshToken(member);
        member.updateRefreshToken(refreshToken);
        return refreshToken;
    }
    public void clearRefreshToken(Member member) {
        member.clearRefreshToken();
    }

    public boolean isValidRefreshToken(String refreshToken) {
        return authTokenService.isValid(refreshToken);
    }
    public Map<String,Object> getRefreshTokenPayload(String refreshToken) {
        return authTokenService.payload(refreshToken);
    }

    public void deleteMember(Member member) { memberRepository.delete(member); }
}
