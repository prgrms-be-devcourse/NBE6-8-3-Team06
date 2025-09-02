package com.back.global.security

import com.back.domain.member.member.service.MemberService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class OAuth2ServiceTest {

    @Autowired
    private lateinit var memberService: MemberService

    @Test
    @DisplayName("카카오 OAuth2 - modifyOrJoin 패턴 (email 기반)")
    fun `카카오_OAuth2_modifyOrJoin_email_기반`() {
        // given
        val oauthEmail = "kakao_12345@oauth.local" // email 기반
        val password = ""
        val nickname = "카카오테스트"
        val profileImgUrl = "https://k.kakaocdn.net/dn/profile.jpg"

        // when - 첫 번째 로그인 (회원가입)
        val result1 = memberService.modifyOrJoin(oauthEmail, password, nickname, profileImgUrl)

        // then
        assert(result1.resultCode == "201-1") // 회원가입 완료
        assert(result1.msg == "회원가입이 완료되었습니다.")
        assert(result1.data != null)
        assert(result1.data!!.getEmail() == oauthEmail)
        assert(result1.data!!.getName() == nickname)
        assert(result1.data!!.profileImgUrl == profileImgUrl)

        // when - 두 번째 로그인 (정보 수정)
        val updatedNickname = "수정된닉네임"
        val updatedProfileImg = "https://k.kakaocdn.net/dn/updated.jpg"
        val result2 = memberService.modifyOrJoin(oauthEmail, password, updatedNickname, updatedProfileImg)

        // then
        assert(result2.resultCode == "200-1") // 회원 정보가 수정되었습니다
        assert(result2.msg == "회원 정보가 수정되었습니다.")
        assert(result2.data != null)
        assert(result2.data!!.id == result1.data!!.id) // 같은 사용자
        assert(result2.data!!.getName() == result1.data!!.getName()) // name은 안 바뀜
        assert(result2.data!!.profileImgUrl == updatedProfileImg) // 프로필 이미지만 업데이트됨
    }

    @Test
    @DisplayName("일반 회원과 OAuth2 회원 구분 (email 패턴으로)")
    fun `일반_회원과_OAuth2_회원_email_패턴_구분`() {
        // given - 일반 회원
        val regularMember = memberService.join("일반회원", "regular@example.com", "password123")
        
        // given - OAuth2 회원 (modifyOrJoin 사용)
        val oauthResult = memberService.modifyOrJoin("kakao_999@oauth.local", "", "카카오회원", null)
        val oauthMember = oauthResult.data!!

        // then
        // 이메일 패턴으로 구분
        assert(!regularMember.getEmail().contains("@oauth.local"))
        assert(oauthMember.getEmail().contains("@oauth.local"))
        assert(oauthMember.getEmail().startsWith("kakao_"))
        
        // 비밀번호로 구분 
        assert(regularMember.getPassword().isNotEmpty())
        assert(oauthMember.getPassword().isEmpty())
        
        // 둘 다 동일한 방식으로 토큰 생성 가능
        val regularToken = memberService.geneAccessToken(regularMember)
        val oauthToken = memberService.geneAccessToken(oauthMember)
        
        assert(regularToken.isNotEmpty())
        assert(oauthToken.isNotEmpty())
    }

    @Test
    @DisplayName("RsData 패턴 확인")
    fun `RsData_패턴_확인`() {
        // given
        val email = "kakao_rsdata_test@oauth.local"
        val nickname = "RsData테스트"

        // when - 첫 번째 호출 (가입)
        val joinResult = memberService.modifyOrJoin(email, "", nickname, null)
        
        // when - 두 번째 호출 (수정)  
        val modifyResult = memberService.modifyOrJoin(email, "", "수정된닉네임", null)

        // then
        assert(joinResult.statusCode == 201) // statusCode 확인
        assert(joinResult.resultCode == "201-1")
        
        assert(modifyResult.statusCode == 200) // statusCode 확인
        assert(modifyResult.resultCode == "200-1")
        
        assert(joinResult.data != null)
        assert(modifyResult.data != null)
        assert(joinResult.data!!.id == modifyResult.data!!.id) // 같은 사용자
    }

    @Test
    @DisplayName("OAuth2 사용자 토큰 생성 테스트")
    fun `OAuth2_사용자_토큰_생성`() {
        // given
        val email = "kakao_67890@oauth.local"
        val nickname = "토큰테스트"
        val memberResult = memberService.modifyOrJoin(email, "", nickname, null)
        val member = memberResult.data!!

        // when
        val accessToken = memberService.geneAccessToken(member)

        // then
        assert(accessToken.isNotEmpty())
        
        // 토큰 파싱해서 확인 (payload 메서드 존재한다면)
        val payload = memberService.payload(accessToken)
        assert(payload != null)
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
}
