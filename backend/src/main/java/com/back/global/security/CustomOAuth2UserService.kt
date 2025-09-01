package com.back.global.security

import com.back.domain.member.member.service.MemberService
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private enum class OAuth2Provider {
    KAKAO, GOOGLE;

    companion object {
        fun from(registrationId: String): OAuth2Provider =
            entries.firstOrNull { it.name.equals(registrationId, ignoreCase = true) }
                ?: error("Unsupported provider: $registrationId")
    }
}

@Service
class CustomOAuth2UserService(
    private val memberService: MemberService
) : DefaultOAuth2UserService() {

    private val logger = LoggerFactory.getLogger(CustomOAuth2UserService::class.java)

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        logger.debug("=== OAuth2UserService 시작 ===")
        
        try {
            val oAuth2User = super.loadUser(userRequest)
            logger.debug("카카오 사용자 정보 받음: {}", oAuth2User.attributes)
            
            val provider = OAuth2Provider.from(userRequest.clientRegistration.registrationId)
            logger.debug("OAuth2 Provider: {}", provider.name)

            val (oauthUserId, nickname, profileImgUrl) = when (provider) {
                OAuth2Provider.KAKAO -> {
                    val props = (oAuth2User.attributes["properties"] as? Map<String, Any>) 
                        ?: error("카카오 사용자 정보를 찾을 수 없습니다.")
                    
                    logger.debug("카카오 properties: {}", props)
                    
                    Triple(
                        oAuth2User.name, // 카카오 사용자 ID
                        props["nickname"] as String,
                        props["profile_image"] as? String
                    )
                }

                OAuth2Provider.GOOGLE -> {
                    val attrs = oAuth2User.attributes
                    Triple(
                        oAuth2User.name,
                        attrs["name"] as String,
                        attrs["picture"] as? String
                    )
                }
            }

            logger.debug("파싱된 정보 - ID: {}, 닉네임: {}, 프로필 이미지: {}", oauthUserId, nickname, profileImgUrl)

            // 현재 email 기반 시스템에 맞게: kakao_12345@oauth.local 형태로 email 생성
            val oauthEmail = "${provider.name.lowercase()}_${oauthUserId}@oauth.local"
            val password = "" // OAuth2 사용자는 빈 비밀번호
            
            logger.debug("생성된 OAuth 이메일: {}", oauthEmail)

            // 기존 회원 찾기
            val existingMember = memberService.findByEmail(oauthEmail)
            
            if (existingMember == null) {
                // 새 회원인 경우에만 가입 처리
                logger.debug("새 OAuth2 회원 가입 처리")
                val memberResult = memberService.modifyOrJoin(oauthEmail, password, nickname, profileImgUrl)
                logger.debug("modifyOrJoin 결과: {}", memberResult.resultCode)
                
                val member = memberResult.data 
                    ?: error("OAuth2 회원 생성에 실패했습니다.")
                
                logger.debug("SecurityUser 생성")
                return SecurityUser.create(member, oAuth2User.attributes)
            } else {
                // 기존 회원인 경우 정보만 업데이트
                logger.debug("기존 OAuth2 회원 정보 업데이트")
                memberService.modify(existingMember, nickname, profileImgUrl)
                
                return SecurityUser.create(existingMember, oAuth2User.attributes)
            }
            
        } catch (e: Exception) {
            logger.error("OAuth2UserService에서 에러 발생", e)
            throw e
        }
    }
}
