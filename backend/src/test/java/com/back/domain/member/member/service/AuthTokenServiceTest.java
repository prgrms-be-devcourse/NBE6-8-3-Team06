package com.back.domain.member.member.service;

import com.back.domain.member.member.service.AuthTokenService;
import com.back.domain.member.member.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthTokenService 테스트")
class AuthTokenServiceTest {

    @InjectMocks
    private AuthTokenService authTokenService;

    private Member testMember;
    private String jwtSecretKey = "testSecretKey545348354897892318523489523445964345";
    private int accessTokenExpSec = 3600;
    private int refreshTokenExpSec = 60 * 60 * 24;

    @BeforeEach
    void setUp() {
        // private 필드에 값 주입
        ReflectionTestUtils.setField(authTokenService, "jwtSecretKey", jwtSecretKey);
        ReflectionTestUtils.setField(authTokenService, "accessTokenExpSec", accessTokenExpSec);
        ReflectionTestUtils.setField(authTokenService, "refreshTokenExpSec", refreshTokenExpSec);

        // 테스트용 User 객체 생성
        testMember = new Member("testuser", "test@example.com", "password");
        // BaseEntity의 ID 설정을 위해 리플렉션 사용
        ReflectionTestUtils.setField(testMember, "id", 1);
    }

    @Test
    @DisplayName("JWT 토큰 생성 성공 테스트")
    void t1() {
        // genAccessToken은 package-private이므로 리플렉션 사용
        String accessToken = ReflectionTestUtils.invokeMethod(authTokenService, "genAccessToken", testMember);


        assertThat(accessToken).isNotNull();
        assertThat(accessToken).isNotEmpty();

        // 생성된 토큰이 유효한지 확인
        assertThat(authTokenService.isValid(accessToken)).isTrue();

        // payload 확인
        Map<String, Object> payload = authTokenService.payload(accessToken);
        assertThat(payload).isNotNull();
        assertThat(payload.get("id")).isEqualTo(testMember.getId());
        assertThat(payload.get("email")).isEqualTo(testMember.getEmail());
    }

    @Test
    @DisplayName("유효한 JWT 토큰 검증 성공 테스트")
    void t2() {

        String validToken = ReflectionTestUtils.invokeMethod(authTokenService, "genAccessToken", testMember);


        boolean isValid = authTokenService.isValid(validToken);


        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("잘못된 JWT 토큰 검증 실패 테스트")
    void t3() {

        String invalidToken = "invalid.jwt.token";


        boolean isValid = authTokenService.isValid(invalidToken);


        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("만료된 JWT 토큰 검증 실패 테스트")
    void t4() {

        // 만료 시간을 1초로 설정하여 즉시 만료되는 토큰 생성
        ReflectionTestUtils.setField(authTokenService, "accessTokenExpSec", 1);
        String expiredToken = ReflectionTestUtils.invokeMethod(authTokenService, "genAccessToken", testMember);

        // 토큰이 만료되도록 2초 대기
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }


        boolean isValid = authTokenService.isValid(expiredToken);


        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("유효한 토큰에서 payload 추출 성공 테스트")
    void t5() {

        String validToken = ReflectionTestUtils.invokeMethod(authTokenService, "genAccessToken", testMember);


        Map<String, Object> payload = authTokenService.payload(validToken);


        assertThat(payload).isNotNull();
        assertThat(payload).hasSize(2);
        assertThat(payload.get("id")).isEqualTo(testMember.getId());
        assertThat(payload.get("email")).isEqualTo(testMember.getEmail());
    }

    @Test
    @DisplayName("잘못된 토큰에서 payload 추출 실패 테스트")
    void t6() {

        String invalidToken = "invalid.jwt.token";


        Map<String, Object> payload = authTokenService.payload(invalidToken);


        assertThat(payload).isNull();
    }

    @Test
    @DisplayName("다른 시크릿 키로 서명된 토큰 검증 실패 테스트")
    void t7() {

        String validToken = ReflectionTestUtils.invokeMethod(authTokenService, "genAccessToken", testMember);

        // 다른 시크릿 키로 변경
        ReflectionTestUtils.setField(authTokenService, "jwtSecretKey", "differentSecretKey249842348974897988656456");


        boolean isValid = authTokenService.isValid(validToken);


        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Refresh Token 생성 성공 테스트")
    void t8() {
        // RefreshToken 생성
        String refreshToken = authTokenService.genRefreshToken(testMember);

        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();

        // 생성된 Refresh Token이 유효한지 확인
        assertThat(authTokenService.isValid(refreshToken)).isTrue();

        // payload 확인
        Map<String, Object> payload = authTokenService.payload(refreshToken);
        assertThat(payload).isNotNull();
        assertThat(payload.get("id")).isEqualTo(testMember.getId());
        assertThat(payload.get("email")).isEqualTo(testMember.getEmail());
    }

    @Test
    @DisplayName("Refresh Token 유효성 검증 및 payload 추출 성공 테스트")
    void t9() {
        // RefreshToken 생성
        String refreshToken = authTokenService.genRefreshToken(testMember);

        // 유효성 검증
        boolean isValid = authTokenService.isValid(refreshToken);
        assertThat(isValid).isTrue();

        // payload 추출
        Map<String, Object> payload = authTokenService.payload(refreshToken);
        assertThat(payload).isNotNull();
        assertThat(payload).hasSize(2);
        assertThat(payload.get("id")).isEqualTo(testMember.getId());
        assertThat(payload.get("email")).isEqualTo(testMember.getEmail());
    }

}