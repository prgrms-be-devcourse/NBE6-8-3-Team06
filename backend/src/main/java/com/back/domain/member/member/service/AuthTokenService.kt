package com.back.domain.member.member.service

import com.back.domain.member.member.entity.Member
import com.back.global.standard.util.Ut
import org.springframework.beans.factory.annotation.Value

import org.springframework.stereotype.Service
import java.util.Map

@Service
class AuthTokenService(
    @Value("\${custom.jwt.secretKey}")
    private val jwtSecretKey: String,
    @Value("\${custom.accessToken.expirationSeconds}")
    private val accessTokenExpSec: Int,
    @Value("\${custom.refreshToken.expirationSeconds}")
    private val refreshTokenExpSec: Int
) {
   fun genAccessToken(member: Member): String? {
        val payload = mapOf(
            "id" to member.id,
            "email" to member.getEmail()
        )

        return Ut.jwt.toString(
            jwtSecretKey,
            accessTokenExpSec,
            payload
        )
    }

    fun genRefreshToken(member: Member): String? {
        val payload = mapOf(
            "id" to member.id,
            "email" to member.getEmail()
        )

        return Ut.jwt.toString(
            jwtSecretKey,
            refreshTokenExpSec,
            payload
        )
    }


    fun payload(accessToken: String): MutableMap<String, Any>? {
        val parsedPayload = Ut.jwt.payload(jwtSecretKey, accessToken)
            ?: return null

        return mutableMapOf(
            "id" to parsedPayload["id"] as Int,
            "email" to parsedPayload["email"] as String
        )
    }

    fun isValid(accessToken: String): Boolean {
        return Ut.jwt.isValid(jwtSecretKey, accessToken)
    }
}
