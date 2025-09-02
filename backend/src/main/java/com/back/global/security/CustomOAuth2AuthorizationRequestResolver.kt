package com.back.global.security

import com.back.standard.extensions.base64Encode
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component
import java.util.*

@Component
class CustomOAuth2AuthorizationRequestResolver(
    private val clientRegistrationRepository: ClientRegistrationRepository,
) : OAuth2AuthorizationRequestResolver {

    private val delegate = DefaultOAuth2AuthorizationRequestResolver(
        clientRegistrationRepository,
        OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI,
    )

    override fun resolve(request: HttpServletRequest): OAuth2AuthorizationRequest? =
        delegate.resolve(request)?.let { customizeState(it, request) }

    override fun resolve(request: HttpServletRequest, clientRegistrationId: String?): OAuth2AuthorizationRequest? =
        delegate.resolve(request, clientRegistrationId)?.let { customizeState(it, request) }

    private fun customizeState(
        req: OAuth2AuthorizationRequest,
        request: HttpServletRequest,
    ): OAuth2AuthorizationRequest {
        val redirectUrl = request.getParameter("redirectUrl").orEmpty().ifBlank { "/" }
        val originState = UUID.randomUUID().toString()
        val rawState = "$redirectUrl#$originState"
        val encodedState = rawState.base64Encode()

        return OAuth2AuthorizationRequest.from(req)
            .state(encodedState)
            .build()
    }
}