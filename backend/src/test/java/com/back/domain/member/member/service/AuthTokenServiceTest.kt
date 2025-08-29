package com.back.domain.member.member.service

import com.back.domain.member.member.entity.Member
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils

@ExtendWith(MockitoExtension::class)
@DisplayName("AuthTokenService 테스트")
internal class AuthTokenServiceTest {
    @InjectMocks
    private val authTokenService: AuthTokenService? = null

    private var testMember: Member? = null
    private val jwtSecretKey = "testSecretKey545348354897892318523489523445964345"
    private val accessTokenExpSec = 3600
    private val refreshTokenExpSec = 60 * 60 * 24

    @BeforeEach
    fun setUp() {
        // private 필드에 값 주입
        ReflectionTestUtils.setField(authTokenService!!, "jwtSecretKey", jwtSecretKey)
        ReflectionTestUtils.setField(authTokenService, "accessTokenExpSec", accessTokenExpSec)
        ReflectionTestUtils.setField(authTokenService, "refreshTokenExpSec", refreshTokenExpSec)

        // 테스트용 User 객체 생성
        testMember = Member("testuser", "test@example.com", "password")
        // BaseEntity의 ID 설정을 위해 리플렉션 사용
        ReflectionTestUtils.setField(testMember!!, "id", 1)
    }

    @Test
    @DisplayName("JWT 토큰 생성 성공 테스트")
    fun t1() {
        // genAccessToken은 package-private이므로 리플렉션 사용
        val accessToken = ReflectionTestUtils.invokeMethod<String?>(authTokenService!!, "genAccessToken", testMember)


        Assertions.assertThat(accessToken).isNotNull()
        Assertions.assertThat(accessToken).isNotEmpty()

        // 생성된 토큰이 유효한지 확인
        Assertions.assertThat(authTokenService.isValid(accessToken!!)).isTrue()

        // payload 확인
        val payload: MutableMap<String?, Any?>? = authTokenService.payload(accessToken)
        Assertions.assertThat<String?, Any?>(payload).isNotNull()
        Assertions.assertThat<Any?>(payload!!.get("id")).isEqualTo(testMember!!.id)
        Assertions.assertThat<Any?>(payload.get("email")).isEqualTo(testMember!!.getEmail())
    }

    @Test
    @DisplayName("유효한 JWT 토큰 검증 성공 테스트")
    fun t2() {
        val validToken = ReflectionTestUtils.invokeMethod<String?>(authTokenService!!, "genAccessToken", testMember)


        val isValid = authTokenService.isValid(validToken!!)


        Assertions.assertThat(isValid).isTrue()
    }

    @Test
    @DisplayName("잘못된 JWT 토큰 검증 실패 테스트")
    fun t3() {
        val invalidToken = "invalid.jwt.token"


        val isValid = authTokenService!!.isValid(invalidToken)


        Assertions.assertThat(isValid).isFalse()
    }

    @Test
    @DisplayName("만료된 JWT 토큰 검증 실패 테스트")
    fun t4() {
        // 만료 시간을 1초로 설정하여 즉시 만료되는 토큰 생성

        ReflectionTestUtils.setField(authTokenService!!, "accessTokenExpSec", 1)
        val expiredToken = ReflectionTestUtils.invokeMethod<String?>(authTokenService, "genAccessToken", testMember)

        // 토큰이 만료되도록 2초 대기
        try {
            Thread.sleep(2000)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }


        val isValid = authTokenService.isValid(expiredToken!!)


        Assertions.assertThat(isValid).isFalse()
    }

    @Test
    @DisplayName("유효한 토큰에서 payload 추출 성공 테스트")
    fun t5() {
        val validToken = ReflectionTestUtils.invokeMethod<String?>(authTokenService!!, "genAccessToken", testMember)


        val payload: MutableMap<String?, Any?>? = authTokenService.payload(validToken!!)


        Assertions.assertThat<String?, Any?>(payload).isNotNull()
        Assertions.assertThat<String?, Any?>(payload).hasSize(2)
        Assertions.assertThat<Any?>(payload!!.get("id")).isEqualTo(testMember!!.id)
        Assertions.assertThat<Any?>(payload.get("email")).isEqualTo(testMember!!.getEmail())
    }

    @Test
    @DisplayName("잘못된 토큰에서 payload 추출 실패 테스트")
    fun t6() {
        val invalidToken = "invalid.jwt.token"


        val payload: MutableMap<String?, Any?>? = authTokenService!!.payload(invalidToken)


        Assertions.assertThat<String?, Any?>(payload).isNull()
    }

    @Test
    @DisplayName("다른 시크릿 키로 서명된 토큰 검증 실패 테스트")
    fun t7() {
        val validToken = ReflectionTestUtils.invokeMethod<String?>(authTokenService!!, "genAccessToken", testMember)

        // 다른 시크릿 키로 변경
        ReflectionTestUtils.setField(authTokenService, "jwtSecretKey", "differentSecretKey249842348974897988656456")


        val isValid = authTokenService.isValid(validToken!!)


        Assertions.assertThat(isValid).isFalse()
    }

    @Test
    @DisplayName("Refresh Token 생성 성공 테스트")
    fun t8() {
        // RefreshToken 생성
        val refreshToken = authTokenService!!.genRefreshToken(testMember!!)

        Assertions.assertThat(refreshToken).isNotNull()
        Assertions.assertThat(refreshToken).isNotEmpty()

        // 생성된 Refresh Token이 유효한지 확인
        Assertions.assertThat(authTokenService.isValid(refreshToken!!)).isTrue()

        // payload 확인
        val payload: MutableMap<String?, Any?>? = authTokenService.payload(refreshToken)
        Assertions.assertThat<String?, Any?>(payload).isNotNull()
        Assertions.assertThat<Any?>(payload!!.get("id")).isEqualTo(testMember!!.id)
        Assertions.assertThat<Any?>(payload.get("email")).isEqualTo(testMember!!.getEmail())
    }

    @Test
    @DisplayName("Refresh Token 유효성 검증 및 payload 추출 성공 테스트")
    fun t9() {
        // RefreshToken 생성
        val refreshToken = authTokenService!!.genRefreshToken(testMember!!)

        // 유효성 검증
        val isValid = authTokenService.isValid(refreshToken!!)
        Assertions.assertThat(isValid).isTrue()

        // payload 추출
        val payload: MutableMap<String?, Any?>? = authTokenService.payload(refreshToken)
        Assertions.assertThat<String?, Any?>(payload).isNotNull()
        Assertions.assertThat<String?, Any?>(payload).hasSize(2)
        Assertions.assertThat<Any?>(payload!!.get("id")).isEqualTo(testMember!!.id)
        Assertions.assertThat<Any?>(payload.get("email")).isEqualTo(testMember!!.getEmail())
    }
}