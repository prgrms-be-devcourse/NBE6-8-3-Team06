package com.back.global.standard.util

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.*
import javax.crypto.SecretKey

private fun String.toSecretKey(): SecretKey = Keys.hmacShaKeyFor(this.toByteArray())

class Ut {
    object jwt {

        fun toString(
            secret: String,
            expirationSec: Int,
            body: Map<String, Any>
        ): String? {
            return try{
                val secretKey = secret.toSecretKey()
                val now = Date()
                val expiration = Date(now.time + expirationSec * 1000L )

                val claims = Jwts.claims().apply {
                    body.forEach { (key,value) -> add(key, value) }
                }.build()

                Jwts.builder()
                    .claims(claims)
                    .issuedAt(now)
                    .expiration(expiration)
                    .signWith(secretKey)
                    .compact()
            } catch (e: Exception){
                null
            }
        }

        @JvmStatic
        fun payload(secret: String, jwtStr: String?): Map<String, Any>? {
            if(jwtStr.isNullOrBlank()) return null

            return try {
                val secretKey = secret.toSecretKey()
                val claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwtStr)
                    .payload

                claims.entries
                    .filter { it.key != null && it.value != null }
                    .associate { it.key to it.value!! }
            } catch (e: Exception) {
                return null
            }
        }

        @JvmStatic
        fun isValid(secret: String, jwtStr: String?): Boolean {
            if(jwtStr.isNullOrBlank()) return false

                return try {
                    val secretKey = secret.toSecretKey()
                    Jwts
                        .parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(jwtStr)
                    true
                } catch (e: Exception) {
                    return false
                }
        }
    }
}
