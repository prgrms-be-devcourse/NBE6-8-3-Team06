package com.back.domain.auth.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/oauth2")
class OAuth2Controller {

    @GetMapping("/success")
    fun oauthSuccess(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "message" to "OAuth2 로그인 성공!"
        ))
    }

    @GetMapping("/failure") 
    fun oauthFailure(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf(
            "status" to "error",
            "message" to "OAuth2 로그인 실패. 백엔드 콘솔 로그를 확인하세요."
        ))
    }
}
