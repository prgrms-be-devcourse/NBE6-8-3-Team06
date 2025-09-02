package com.back.domain.member.member.service

import com.back.domain.member.member.entity.Member
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils


@DisplayName("AuthTokenService 테스트")
internal class AuthTokenServiceTest {

    private lateinit var authTokenService: AuthTokenService
    private lateinit var testMember: Member

    private val jwtSecretKey = "testSecretKey545348354897892318523489523445964345"
    private val accessTokenExpSec = 3600
    private val refreshTokenExpSec = 60 * 60 * 24

    @BeforeEach
    fun setUp() {
        authTokenService = AuthTokenService(jwtSecretKey, accessTokenExpSec, refreshTokenExpSec)

        testMember = Member("testuser", "test@example.com", "password").apply {
            ReflectionTestUtils.setField(this, "id", 1)
        }
    }

    @Test
    @DisplayName("JWT 액세스 토큰 생성")
    fun t1() {
        // when
        val accessToken = authTokenService.genAccessToken(testMember)

        // then
        assertThat(accessToken).isNotNull
        assertThat(accessToken).isNotEmpty
        assertThat(authTokenService.isValid(accessToken!!)).isTrue

        val payload = authTokenService.payload(accessToken)
        assertThat(payload).isNotNull
        assertThat(payload!!["id"]).isEqualTo(testMember.id)
        assertThat(payload["email"]).isEqualTo(testMember.getEmail())
    }

    @Test
    @DisplayName("JWT 리프레시 토큰 생성")
    fun t2() {
        // when
        val refreshToken = authTokenService.genRefreshToken(testMember)

        // then
        assertThat(refreshToken).isNotNull
        assertThat(refreshToken).isNotEmpty
        assertThat(authTokenService.isValid(refreshToken!!)).isTrue

        val payload = authTokenService.payload(refreshToken)
        assertThat(payload).isNotNull
        assertThat(payload!!).hasSize(2)
        assertThat(payload["id"]).isEqualTo(testMember.id)
        assertThat(payload["email"]).isEqualTo(testMember.getEmail())
    }

    @Test
    @DisplayName("유효한 JWT 토큰 검증")
    fun t3() {
        // given
        val validToken = authTokenService.genAccessToken(testMember)

        // when
        val isValid = authTokenService.isValid(validToken!!)

        // then
        assertThat(isValid).isTrue
    }

    @Test
    @DisplayName("잘못된 JWT 토큰 검증 실패")
    fun t4() {
        // given
        val invalidToken = "invalid.jwt.token"

        // when
        val isValid = authTokenService.isValid(invalidToken)

        // then
        assertThat(isValid).isFalse
    }

    @Test
    @DisplayName("만료된 JWT 토큰 검증 실패")
    fun t5() {
        // given - 1초로 설정한 짧은 만료시간의 서비스
        val shortExpService = AuthTokenService(jwtSecretKey, 1, refreshTokenExpSec)
        val expiredToken = shortExpService.genAccessToken(testMember)

        // when - 토큰 만료를 위해 2초 대기
        Thread.sleep(2000)
        val isValid = shortExpService.isValid(expiredToken!!)

        // then
        assertThat(isValid).isFalse
    }

    @Test
    @DisplayName("유효한 토큰에서 payload 추출")
    fun t6() {
        // given
        val validToken = authTokenService.genAccessToken(testMember)

        // when
        val payload = authTokenService.payload(validToken!!)

        // then
        assertThat(payload).isNotNull
        assertThat(payload!!).hasSize(2)
        assertThat(payload["id"]).isEqualTo(testMember.id)
        assertThat(payload["email"]).isEqualTo(testMember.getEmail())
    }

    @Test
    @DisplayName("잘못된 토큰에서 payload 추출 실패")
    fun t7() {
        // given
        val invalidToken = "invalid.jwt.token"

        // when
        val payload = authTokenService.payload(invalidToken)

        // then
        assertThat(payload).isNull()
    }

    @Test
    @DisplayName("다른 시크릿 키로 서명된 토큰 검증 실패")
    fun t8() {
        // given
        val validToken = authTokenService.genAccessToken(testMember)
        val differentKeyService = AuthTokenService("differentSecretKey249842348974897988656456", accessTokenExpSec, refreshTokenExpSec)

        // when
        val isValid = differentKeyService.isValid(validToken!!)

        // then
        assertThat(isValid).isFalse
    }

    @Test
    @DisplayName("액세스 토큰과 리프레시 토큰 동시 생성 및 검증")
    fun t9() {
        // when
        val accessToken = authTokenService.genAccessToken(testMember)
        val refreshToken = authTokenService.genRefreshToken(testMember)

        // then
        assertThat(accessToken).isNotNull
        assertThat(refreshToken).isNotNull
        assertThat(accessToken).isNotEqualTo(refreshToken)

        assertThat(authTokenService.isValid(accessToken!!)).isTrue
        assertThat(authTokenService.isValid(refreshToken!!)).isTrue

        // 두 토큰의 payload가 같은지 확인
        val accessPayload = authTokenService.payload(accessToken)
        val refreshPayload = authTokenService.payload(refreshToken)

        assertThat(accessPayload).isEqualTo(refreshPayload)
    }
}