package com.back.domain.member.member.controller

import com.back.domain.member.member.dto.MemberJoinReqDto
import com.back.domain.member.member.dto.MemberLoginReqDto
import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.repository.MemberRepository
import com.back.domain.member.member.service.MemberService
import com.back.global.rq.Rq
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import jakarta.servlet.http.Cookie

internal class MemberControllerTest {

    private val memberService = mockk<MemberService>()
    private val memberRepository = mockk<MemberRepository>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val rq = mockk<Rq>()

    private lateinit var memberController: MemberController
    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        memberController = MemberController(memberService, passwordEncoder, rq)
        mockMvc = MockMvcBuilders.standaloneSetup(memberController).build()
        objectMapper = ObjectMapper()
    }

    @Test
    @DisplayName("회원가입 성공")
    fun t1() {
        // given
        val email = "test@example.com"
        val name = "TestUser"
        val password = "password123"
        val encodedPassword = "encodedPassword123"

        val reqBody = MemberJoinReqDto(email, name, password)
        val newMember = Member(name, email, encodedPassword)

        every { memberService.findByEmail(email) } returns null
        every { passwordEncoder.encode(password) } returns encodedPassword
        every { memberService.join(name, email, encodedPassword) } returns newMember

        // when & then
        mockMvc.post("/user/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(reqBody)
        }.andExpect {
            status { isOk() }
            jsonPath("$.resultCode") { value("201-1") }
            jsonPath("$.msg") { value("${name}님 환영합니다. Bookers 회원가입이 완료되었습니다.") }
            jsonPath("$.data.email") { value(email) }
            jsonPath("$.data.name") { value(name) }
        }

        verify(exactly = 1) {
            memberService.findByEmail(email)
            passwordEncoder.encode(password)
            memberService.join(name, email, encodedPassword)
        }
    }

    @Test
    @DisplayName("로그인 성공")
    fun t2() {
        // given
        val email = "test@example.com"
        val password = "password123"
        val encodedPassword = "encodedPassword123"
        val accessToken = "mockAccessToken"
        val refreshToken = "mockRefreshToken"

        val reqBody = MemberLoginReqDto(email, password)
        val existingMember = Member("TestUser", email, encodedPassword)

        every { memberService.findByEmail(email) } returns existingMember
        every { memberService.checkPassword(existingMember, password) } just Runs
        every { memberService.geneAccessToken(existingMember) } returns accessToken
        every { memberService.geneRefreshToken(existingMember) } returns refreshToken
        every { memberService.save(any()) } returns existingMember
        every { rq.setCookie(any(), any()) } just Runs

        // when & then
        mockMvc.post("/user/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(reqBody)
        }.andExpect {
            status { isOk() }
            jsonPath("$.resultCode") { value("200-1") }
            jsonPath("$.msg") { value("${email}님 환영합니다.") }
            jsonPath("$.data.memberDto.email") { value(email) }
            jsonPath("$.data.accessToken") { value(accessToken) }
        }

        verify(exactly = 1) {
            memberService.findByEmail(email)
            memberService.checkPassword(existingMember, password)
            memberService.geneAccessToken(existingMember)
            memberService.geneRefreshToken(existingMember)
            memberService.save(any())
            rq.setCookie("accessToken", accessToken)
            rq.setCookie("refreshToken", refreshToken)
        }
    }

    @Test
    @DisplayName("Access Token 재발급 성공")
    fun t3() {
        // given
        val email = "test@example.com"
        val existingRefreshToken = "existingMockRefreshToken"
        val newAccessToken = "newMockAccessToken"

        val existingMember = Member("TestUser", email, "encodedPassword123").apply {
            ReflectionTestUtils.setField(this, "id", 1)
            ReflectionTestUtils.setField(this, "refreshToken", existingRefreshToken)
        }

        val tokenPayload = mapOf(
            "id" to 1,
            "email" to email
        )

        every { memberService.isValidRefreshToken(existingRefreshToken) } returns true
        every { memberService.getRefreshTokenPayload(existingRefreshToken) } returns tokenPayload
        every { memberService.findByEmail(email) } returns existingMember
        every { memberService.geneAccessToken(existingMember) } returns newAccessToken
        every { rq.setCookie(any(), any()) } just Runs

        // when & then
        mockMvc.post("/user/reissue") {
            cookie(Cookie("refresh_token", existingRefreshToken))
        }.andExpect {
            status { isOk() }
            jsonPath("$.resultCode") { value("200") }
            jsonPath("$.msg") { value("AccessToken이 재발급되었습니다.") }
        }

        verify(exactly = 1) {
            memberService.isValidRefreshToken(existingRefreshToken)
            memberService.getRefreshTokenPayload(existingRefreshToken)
            memberService.findByEmail(email)
            memberService.geneAccessToken(existingMember)
            rq.setCookie("accessToken", newAccessToken)
        }

        verify(exactly = 0) {
            memberService.geneRefreshToken(any<Member>())
            memberRepository.save(any<Member>())
            rq.setCookie("refreshToken", any())
        }
    }

    @Test
    @DisplayName("회원탈퇴 성공")
    fun t4() {
        // given
        val email = "test@example.com"
        val name = "TestUser"
        val existingMember = Member(name, email, "encodedPassword123")

        every { rq.actor } returns existingMember
        every { memberService.deleteMember(existingMember) } just Runs
        every { rq.clearAuthCookies() } just Runs

        // when & then
        mockMvc.delete("/user/my").andExpect {
            status { isOk() }
            jsonPath("$.resultCode") { value("200-1") }
            jsonPath("$.msg") { value("회원탈퇴가 완료되었습니다.") }
            jsonPath("$.data") { isEmpty() }
        }

        verify(exactly = 1) {
            rq.actor
            memberService.deleteMember(existingMember)
            rq.clearAuthCookies()
        }
    }
}