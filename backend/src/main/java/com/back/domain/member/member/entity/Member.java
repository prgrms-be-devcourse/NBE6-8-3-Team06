package com.back.domain.member.member.entity;

import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.note.entity.Note;
import com.back.domain.review.review.entity.Review;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Member extends BaseEntity {
    private String name;
    @Column(unique = true)
    private String email;
    private String password;

    @Column(length = 1000)
    private String refreshToken;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Review> reviews;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Bookmark> bookmarks;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Note> notes;

    public Member(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    public void clearRefreshToken() {
        this.refreshToken = null;
    }
}
