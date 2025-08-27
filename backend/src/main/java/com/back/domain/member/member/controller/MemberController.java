package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.dto.MemberJoinReqDto;
import com.back.domain.member.member.dto.MemberLoginReqDto;
import com.back.domain.member.member.dto.MemberLoginResDto;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final Rq rq;

    @PostMapping("/signup")
    @Transactional
    public RsData<MemberDto> join(
            @Valid @RequestBody MemberJoinReqDto reqBody
    ){
        memberService.findByEmail(reqBody.email()).ifPresent(member -> {
            throw new ServiceException("409","이미 존재하는 이메일 입니다. 다시 입력해주세요.");
        });
        Member member = memberService.join(
                reqBody.name(),
                reqBody.email(),
                passwordEncoder.encode(reqBody.password())
        );
        return new RsData<>(
                "201-1",
                "%s님 환영합니다. Bookers 회원가입이 완료되었습니다.".formatted(member.getName()),
                new MemberDto(member)
        );
    }

    @PostMapping("/login")
    @Transactional
    public RsData<MemberLoginResDto> login(
            @Valid @RequestBody MemberLoginReqDto reqBody
    ){
        Member member = memberService.findByEmail(reqBody.email())
                .orElseThrow(()->new ServiceException("401-1", "존재하지 않는 아이디입니다."));

        memberService.checkPassword(member, reqBody.password());

        String accessToken = memberService.geneAccessToken(member);
        String refreshToken = memberService.geneRefreshToken(member);

        member.updateRefreshToken(refreshToken);
        memberService.save(member);

        rq.setCookie("accessToken", accessToken);
        rq.setCookie("refreshToken", refreshToken);

        return new RsData<>(
                "200-1",
                "%s님 환영합니다.".formatted(member.getEmail()),
                new MemberLoginResDto(
                        new MemberDto(member),
                        accessToken
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(){
        Member actor = rq.getActor();

        if(actor != null){
            // 서버에서 refresh 토큰 삭제
            memberService.clearRefreshToken(actor);
        }

        // 쿠키에서 토큰 삭제
        rq.clearAuthCookies();

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<?> getAuthenticatedUser() {
        Member actor = rq.getActor();

        if (actor == null) {
            return ResponseEntity.status(401).body("로그인 상태가 아닙니다."); // 인증되지 않은 사용자에 대한 처리
        }

        return ResponseEntity.ok(new MemberDto(actor));
    }

    @PostMapping("/reissue")
    @Transactional
    public RsData<?> reissue(HttpServletRequest request) {
        String refreshToken = null;

        if (request.getCookies() != null) {
            for(Cookie cookie: request.getCookies()){
                if(cookie.getName().equals("refreshToken")){
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if(refreshToken == null||!memberService.isValidRefreshToken(refreshToken)){
            return new RsData<>("400","유효하지 않은 RefreshToken 입니다.",null);
        }

        Map<String,Object> payload = memberService.getRefreshTokenPayload(refreshToken);
        String email = payload.get("email").toString();

        Member member = memberService.findByEmail(email)
                .orElseThrow(()->new ServiceException("401-1", "사용자를 찾을 수 없습니다."));
        if(!refreshToken.equals(member.getRefreshToken())){
            return new RsData<>("401","서버에 저장된 토큰과 일치하지 않습니다.",null);
        }
        String newAccessToken = memberService.geneAccessToken(member);
        rq.setCookie("accessToken",newAccessToken);

        return new RsData<>("200","AccessToken이 재발급되었습니다.",null);
    }

    @DeleteMapping("/my")
    public RsData<String> deleteMember(HttpServletResponse response) {
        Member actor = rq.getActor();
        if (actor == null) {
            return new RsData<>("401-1","로그인 상태가 아닙니다.",null);
        }

        // 회원 삭제
        memberService.deleteMember(actor);

        // 쿠키에서 토큰 삭제
        rq.clearAuthCookies();

        return new RsData<>("200-1", "회원탈퇴가 완료되었습니다.", null);
    }
}