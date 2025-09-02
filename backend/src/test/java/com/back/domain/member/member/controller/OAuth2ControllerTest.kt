package com.back.domain.member.member.controller

import com.back.domain.member.member.service.MemberService
import com.back.global.security.SecurityUser
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@TestPropertySource(locations = ["classpath:application-test.properties"])
class OAuth2ControllerTest {

    @Autowired
    private lateinit var memberService: MemberService

    @Test
    @DisplayName("modifyOrJoin 패턴 테스트 (OAuth2 없이)")
    fun `modifyOrJoin_패턴_테스트`() {
        // given
        val email = "kakao_modifyorjoin@oauth.local"
        val nickname = "modifyOrJoin테스트"
        val profileImgUrl = "https://example.com/test.jpg"

        // when - 첫 번째 호출 (가입)
        val joinResult = memberService.modifyOrJoin(email, "", nickname, profileImgUrl)
        
        // when - 두 번째 호출 (수정)
        val updatedProfileImg = "https://example.com/updated.jpg"
        val modifyResult = memberService.modifyOrJoin(email, "", nickname, updatedProfileImg)

        // then
        assert(joinResult.resultCode == "201-1") // 회원가입
        assert(joinResult.msg == "회원가입이 완료되었습니다.")
        assert(joinResult.data != null)
        
        assert(modifyResult.resultCode == "200-1") // 회원 정보 수정
        assert(modifyResult.msg == "회원 정보가 수정되었습니다.")
        assert(modifyResult.data != null)
        
        assert(joinResult.data!!.id == modifyResult.data!!.id) // 같은 사용자
        assert(modifyResult.data!!.profileImgUrl == updatedProfileImg) // 프로필 이미지 업데이트됨
    }

    @Test
    @DisplayName("일반 회원과 OAuth2 회원 구분")
    fun `일반_회원과_OAuth2_회원_구분`() {
        // given - 일반 회원
        val regularMember = memberService.join("일반회원", "regular@example.com", "password123")
        
        // given - OAuth2 회원 (modifyOrJoin 사용)
        val oauthResult = memberService.modifyOrJoin("kakao_123@oauth.local", "", "OAuth2회원", "https://example.com/profile.jpg")
        val oauthMember = oauthResult.data!!

        // when & then
        assert(regularMember.getPassword().isNotEmpty()) // 일반 회원은 비밀번호 있음
        assert(oauthMember.getPassword().isEmpty()) // OAuth2 회원은 비밀번호 없음
        
        assert(regularMember.getEmail().contains("@example.com"))
        assert(oauthMember.getEmail().contains("@oauth.local"))
        
        assert(regularMember.profileImgUrl == null) // 일반 회원은 프로필 이미지 없음
        assert(oauthMember.profileImgUrl != null) // OAuth2 회원은 프로필 이미지 있음
    }

    @Test
    @DisplayName("SecurityUser 생성 테스트")
    fun `SecurityUser_생성_테스트`() {
        // given
        val memberResult = memberService.modifyOrJoin("test@oauth.local", "", "테스트", "https://example.com/profile.jpg")
        val member = memberResult.data!!
        val attributes = mapOf(
            "id" to "12345",
            "properties" to mapOf("nickname" to "테스트")
        )

        // when
        val securityUser = SecurityUser.create(member, attributes)

        // then
        assert(securityUser.member == member)
        assert(securityUser.attributes == attributes)
        assert(securityUser.username == member.getEmail())
        assert(securityUser.name == member.id.toString())
        assert(securityUser.authorities.any { it.authority == "ROLE_USER" })
    }

    @Test
    @DisplayName("RsData 응답 코드 확인")
    fun `RsData_응답_코드_확인`() {
        // given
        val email = "kakao_statuscode@oauth.local"
        val nickname = "상태코드테스트"

        // when - 첫 번째 호출 (가입)
        val joinResult = memberService.modifyOrJoin(email, "", nickname, null)
        
        // when - 두 번째 호출 (수정)  
        val modifyResult = memberService.modifyOrJoin(email, "", "수정됨", null)

        // then
        assert(joinResult.statusCode == 201) // HTTP 201 Created
        assert(joinResult.resultCode == "201-1")
        
        assert(modifyResult.statusCode == 200) // HTTP 200 OK
        assert(modifyResult.resultCode == "200-1")
    }

    @Test
    @DisplayName("프로필 이미지 처리")
    fun `프로필_이미지_처리`() {
        // given
        val emailWithImg = "kakao_with_img@oauth.local"
        val emailWithoutImg = "kakao_without_img@oauth.local"
        val profileImg = "https://k.kakaocdn.net/dn/test.jpg"

        // when
        val memberWithImgResult = memberService.modifyOrJoin(emailWithImg, "", "이미지있음", profileImg)
        val memberWithoutImgResult = memberService.modifyOrJoin(emailWithoutImg, "", "이미지없음", null)
        
        val memberWithImg = memberWithImgResult.data!!
        val memberWithoutImg = memberWithoutImgResult.data!!

        // then
        assert(memberWithImg.profileImgUrl == profileImg)
        assert(memberWithImg.profileImgUrlOrDefault == profileImg)
        
        assert(memberWithoutImg.profileImgUrl == null)
        assert(memberWithoutImg.profileImgUrlOrDefault == "https://placehold.co/600x600?text=U_U")
    }

    @Test
    @DisplayName("OAuth2 email 생성 패턴 확인")
    fun `OAuth2_email_생성_패턴_확인`() {
        // given
        val kakaoUserId = "12345"
        val provider = "kakao"
        
        // when - 실제 CustomOAuth2UserService에서 사용하는 패턴
        val oauthEmail = "${provider}_${kakaoUserId}@oauth.local"
        val result = memberService.modifyOrJoin(oauthEmail, "", "패턴테스트", null)

        // then
        assert(result.data != null)
        assert(result.data!!.getEmail() == "kakao_12345@oauth.local")
        assert(result.data!!.getEmail().contains("@oauth.local"))
        assert(result.data!!.getEmail().startsWith("kakao_"))
    }
}
