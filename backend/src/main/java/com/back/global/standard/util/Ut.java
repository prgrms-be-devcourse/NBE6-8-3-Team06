package com.back.global.standard.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.Map;

public class Ut {
    public static class jwt{
        public static String toString(String secret, int expirationSec, Map<String, Object> body ) {
            ClaimsBuilder claimsBuilder = Jwts.claims();

            for(Map.Entry<String, Object> entry : body.entrySet()) {
                claimsBuilder.add(entry.getKey(), entry.getValue());
            }
            Claims claims = claimsBuilder.build();

            Date issueTime = new Date();
            Date expirationTime = new Date(issueTime.getTime() + expirationSec * 1000);

            Key jwtKey = Keys.hmacShaKeyFor(secret.getBytes());
            String jwt = Jwts.builder()
                    .claims(claims)
                    .issuedAt(issueTime)
                    .expiration(expirationTime)
                    .signWith(jwtKey)
                    .compact();
            return jwt;
        }
        public static Map<String,Object> payload(String secret, String jwtStr) {
            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());

            try {
                return (Map<String, Object>) Jwts
                        .parser()
                        .verifyWith(secretKey)
                        .build()
                        .parse(jwtStr)
                        .getPayload();
            } catch (Exception e) {
                return null;
            }
        }
        public static boolean isValid(String secret, String jwtStr) {
            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());
            {
                try {
                    Jwts
                            .parser()
                            .verifyWith(secretKey)
                            .build()
                            .parse(jwtStr);
                } catch (Exception e) {
                    return false;
                }
                return true;
            }
        }
    }
}
