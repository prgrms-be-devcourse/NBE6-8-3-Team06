package com.back.global.security;

import com.back.domain.member.member.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.List;

@Getter
public class SecurityUser extends User {

    private final Member member;

    public SecurityUser(Member member) {
        super(member.getEmail(), member.getPassword(), List.of());
        this.member = member;
    }
}