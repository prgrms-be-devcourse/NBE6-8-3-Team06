package com.back.global.standard.util

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.security.Key
import java.util.*

class Ut {
    object jwt {
        fun toString(secret: String, expirationSec: Int, body: MutableMap<String?, Any?>): String? {
            val claimsBuilder = Jwts.claims()

            for (entry in body.entries) {
                claimsBuilder.add(entry.key, entry.value)
            }
            val claims = claimsBuilder.build()

            val issueTime = Date()
            val expirationTime = Date(issueTime.getTime() + expirationSec * 1000)

            val jwtKey: Key = Keys.hmacShaKeyFor(secret.toByteArray())
            val jwt = Jwts.builder()
                .claims(claims)
                .issuedAt(issueTime)
                .expiration(expirationTime)
                .signWith(jwtKey)
                .compact()
            return jwt
        }

        @JvmStatic
        fun payload(secret: String, jwtStr: String?): MutableMap<String?, Any?>? {
            val secretKey = Keys.hmacShaKeyFor(secret.toByteArray())

            try {
                return Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(jwtStr)
                    .getPayload() as MutableMap<String?, Any?>?
            } catch (e: Exception) {
                return null
            }
        }

        @JvmStatic
        fun isValid(secret: String, jwtStr: String?): Boolean {
            val secretKey = Keys.hmacShaKeyFor(secret.toByteArray())
            run {
                try {
                    Jwts
                        .parser()
                        .verifyWith(secretKey)
                        .build()
                        .parse(jwtStr)
                } catch (e: Exception) {
                    return false
                }
                return true
            }
        }
    }
}
