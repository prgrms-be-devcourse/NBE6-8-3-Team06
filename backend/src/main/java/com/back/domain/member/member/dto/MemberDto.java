package com.back.domain.member.member.dto;

import com.back.domain.member.member.entity.Member;

public record MemberDto (
    String email,
    String name,
    String password
) {
    public MemberDto (String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
    }

    public MemberDto (Member member){
        this(
                member.getEmail(),
                member.getName(),
                member.getPassword()
        );
    }
}
