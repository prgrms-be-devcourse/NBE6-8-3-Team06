package com.back.domain.member.member.controller

import com.back.domain.member.member.dto.MemberJoinReqDto
import com.back.domain.member.member.dto.MemberLoginReqDto
import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.repository.MemberRepository
import com.back.domain.member.member.service.MemberService
import com.back.global.rq.Rq
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.*
import java.util.Map

@ExtendWith(MockitoExtension::class)
internal class MemberControllerTest {
    @InjectMocks
    private val memberController: MemberController? = null

    @Mock
    private val memberService: MemberService? = null

    @Mock
    private val memberRepository: MemberRepository? = null

    @Mock
    private val passwordEncoder: PasswordEncoder? = null

    @Mock
    private val rq: Rq? = null

    private var mockMvc: MockMvc? = null
    private var objectMapper: ObjectMapper? = null

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(memberController).build()
        objectMapper = ObjectMapper()
    }

    @Test
    @DisplayName("회원가입 성공")
    @Throws(Exception::class)
    fun t1() {
        // Given
        val email = "test@example.com"
        val name = "TestUser"
        val password = "password123"
        val encodedPassword = "encodedPassword123"

        val reqBody = MemberJoinReqDto(email, name, password)
        val newMember = Member(name, email, encodedPassword)

        Mockito.`when`<Member?>(memberService!!.findByEmail(email)).thenReturn(Optional.empty<T?>())
        Mockito.`when`<String?>(passwordEncoder!!.encode(password)).thenReturn(encodedPassword)
        Mockito.`when`<Member?>(memberService.join(name, email, encodedPassword)).thenReturn(newMember)

        // When & Then
        mockMvc!!.perform(
            MockMvcRequestBuilders.post("/user/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper!!.writeValueAsString(reqBody))
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201-1"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.msg").value("%s님 환영합니다. Bookers 회원가입이 완료되었습니다.".formatted(name))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(email))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(name))

        Mockito.verify<MemberService?>(memberService, Mockito.times(1)).findByEmail(email)
        Mockito.verify<PasswordEncoder?>(passwordEncoder, Mockito.times(1)).encode(password)
        Mockito.verify<MemberService?>(memberService, Mockito.times(1)).join(name, email, encodedPassword)
    }

    @Test
    @DisplayName("로그인 성공")
    @Throws(Exception::class)
    fun t2() {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val encodedPassword = "encodedPassword123"
        val accessToken = "mockAccessToken"

        val reqBody = MemberLoginReqDto(email, password)
        val existingMember = Member("TestUser", email, encodedPassword)

        Mockito.`when`<Member?>(memberService!!.findByEmail(email)).thenReturn(Optional.of<T?>(existingMember))
        Mockito.doNothing().`when`<MemberService?>(memberService).checkPassword(existingMember, password)
        Mockito.`when`<String?>(memberService.geneAccessToken(existingMember)).thenReturn(accessToken)

        // Mock the setCookie method of Rq
        Mockito.doNothing().`when`<Rq?>(rq).setCookie(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())

        // When & Then
        mockMvc!!.perform(
            MockMvcRequestBuilders.post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper!!.writeValueAsString(reqBody))
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("%s님 환영합니다.".formatted(email)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.memberDto.email").value(email))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.accessToken").value(accessToken))


        Mockito.verify<MemberService?>(memberService, Mockito.times(1)).findByEmail(email)
        Mockito.verify<MemberService?>(memberService, Mockito.times(1)).checkPassword(existingMember, password)
        Mockito.verify<MemberService?>(memberService, Mockito.times(1)).geneAccessToken(existingMember)

        Mockito.verify<Rq?>(rq, Mockito.times(1)).setCookie("accessToken", accessToken)
    }

    @Test
    @DisplayName("Access Token 재발급 성공")
    @Throws(Exception::class)
    fun t3() {
        // Given
        val email = "test@example.com"
        val existingRefreshToken = "existingMockRefreshToken" // 기존 리프레시 토큰
        val newAccessToken = "newMockAccessToken" // 새로 발급될 Access Token

        val existingMember = Member("TestUser", email, "encodedPassword123")
        ReflectionTestUtils.setField(existingMember, "id", 1)
        ReflectionTestUtils.setField(existingMember, "refreshToken", existingRefreshToken) // 기존 리프레시 토큰 설정


        Boolean > Mockito.`when`<Boolean?>(memberService!!.isValidRefreshToken(existingRefreshToken)).thenReturn(true)
        Mockito.`when`<MutableMap<String?, Any?>?>(memberService.getRefreshTokenPayload(existingRefreshToken))
            .thenReturn(
                Map.of<String, Any>("id", 1, "email", email)
            )
        Member > Mockito.`when`<Member?>(memberService.findByEmail(email)).thenReturn(Optional.of<T?>(existingMember))
        String > Mockito.`when`<String?>(memberService.geneAccessToken(existingMember)).thenReturn(newAccessToken)

        Mockito.doNothing().`when`<Rq?>(rq).setCookie(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())

        // When & Then
        mockMvc!!.perform(
            MockMvcRequestBuilders.post("/user/reissue")
                .cookie(Cookie("refreshToken", existingRefreshToken))
        ) // Refresh Token 쿠키 추가
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("AccessToken이 재발급되었습니다."))


        MemberService > Mockito.verify<MemberService?>(memberService, Mockito.times(1))
            .isValidRefreshToken(existingRefreshToken)
        MemberService > Mockito.verify<MemberService?>(memberService, Mockito.times(1))
            .getRefreshTokenPayload(existingRefreshToken)
        MemberService > Mockito.verify<MemberService?>(memberService, Mockito.times(1)).findByEmail(email)
        MemberService > Mockito.verify<MemberService?>(memberService, Mockito.times(1)).geneAccessToken(existingMember)
        MemberService > Mockito.verify<MemberService?>(memberService, Mockito.never())
            .geneRefreshToken(TODO("Cannot convert element"))<Member> ArgumentMatchers . any < com . back . domain . member . member . entity . Member ? > (Member::class.java)

        MemberRepository > Mockito.verify<MemberRepository?>(memberRepository, Mockito.never())
            .save<Member?>(TODO("Cannot convert element"))<Member> ArgumentMatchers . any < com . back . domain . member . member . entity . Member ? > (Member::class.java)


        Rq > Mockito.verify<Rq?>(rq, Mockito.times(1))
            .setCookie(TODO("Cannot convert element"))<String> ArgumentMatchers . eq < kotlin . String ? > ("accessToken")
        String > ArgumentMatchers.eq<String?>(newAccessToken)

        TODO(
            """
            |Cannot convert element
            |With text:
            |Rq>verify(rq, never()).setCookie(<String>eq("refreshToken"), anyString()
            """.trimMargin()
        )
    }

    @Test
    @DisplayName("회원탈퇴 성공")
    @Throws(Exception::class)
    fun t4() {
        // Given
        val email = "test@example.com"
        val name = "TestUser"
        val existingMember = Member(name, email, "encodedPassword123")

        Mockito.`when`<Member?>(rq!!.actor).thenReturn(existingMember)
        Mockito.doNothing().`when`<MemberService?>(memberService).deleteMember(existingMember)

        // When & Then
        mockMvc!!.perform(MockMvcRequestBuilders.delete("/user/my"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("회원탈퇴가 완료되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").isEmpty())

        // Verifications
        Mockito.verify<Rq?>(rq, Mockito.times(1)).actor
        Mockito.verify<MemberService?>(memberService, Mockito.times(1)).deleteMember(existingMember)
    }
}