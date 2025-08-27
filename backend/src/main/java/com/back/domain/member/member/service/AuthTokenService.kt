package com.back.domain.member.member.service

import com.back.domain.member.member.entity.Member
import com.back.global.standard.util.Ut
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Map

@Service
class AuthTokenService {
    @Value("\${custom.jwt.secretKey}")
    private val jwtSecretKey: String? = null

    @Value("\${custom.accessToken.expirationSeconds}")
    private val accessTokenExpSec = 0

    @Value("\${custom.refreshToken.expirationSeconds}")
    private val refreshTokenExpSec = 0

    fun genAccessToken(member: Member): String? {
        val id = member.getId()
        val email: String = member.getEmail()

        return Ut.jwt.toString(
            jwtSecretKey,
            accessTokenExpSec,
            Map.of<String?, Any?>(
                "id", id,
                "email", email
            )
        )
    }

    fun genRefreshToken(member: Member): String? {
        val id = member.getId()
        val email: String = member.getEmail()

        return Ut.jwt.toString(
            jwtSecretKey,
            refreshTokenExpSec,
            Map.of<String?, Any?>(
                "id", id,
                "email", email
            )
        )
    }


    fun payload(accessToken: String?): MutableMap<String?, Any?>? {
        val parsedPayload = Ut.jwt.payload(jwtSecretKey, accessToken)

        if (parsedPayload == null) return null
        val id = parsedPayload.get("id") as Int
        val email = parsedPayload.get("email") as String

        return Map.of<String?, Any?>("id", id, "email", email)
    }

    fun isValid(accessToken: String?): Boolean {
        return Ut.jwt.isValid(jwtSecretKey, accessToken)
    }
}
