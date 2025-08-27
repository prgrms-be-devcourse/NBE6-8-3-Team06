package com.back.domain.member.member.service;

import com.back.domain.member.member.entity.Member;
import com.back.global.standard.util.Ut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.Map;

@Service
public class AuthTokenService {
    @Value("${custom.jwt.secretKey}")
    private String jwtSecretKey;

    @Value("${custom.accessToken.expirationSeconds}")
    private int accessTokenExpSec;

    @Value("${custom.refreshToken.expirationSeconds}")
    private int refreshTokenExpSec;

    public String genAccessToken(Member member) {
        int id = member.getId();
        String email = member.getEmail();

        return Ut.jwt.toString(
                jwtSecretKey,
                accessTokenExpSec,
                Map.of(
                        "id",id,
                        "email",email
                )
        );
    }

    public String genRefreshToken(Member member) {
        int id = member.getId();
        String email = member.getEmail();

        return Ut.jwt.toString(
                jwtSecretKey,
                refreshTokenExpSec,
                Map.of("id", id,
                        "email",email));
    }

    
    public Map<String, Object> payload(String accessToken) {
        Map<String, Object> parsedPayload = Ut.jwt.payload(jwtSecretKey, accessToken);

        if(parsedPayload == null) return null;
        int id = (int) parsedPayload.get("id");
        String email = (String) parsedPayload.get("email");

        return Map.of("id", id, "email", email);
    }
    
    public boolean isValid(String accessToken) { 
        return Ut.jwt.isValid(jwtSecretKey, accessToken); 
    }
}
