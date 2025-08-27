package com.back.domain.review.review.entity;

import com.back.domain.book.book.entity.Book;
import com.back.domain.member.member.entity.Member;
import com.back.domain.review.reviewRecommend.entity.ReviewRecommend;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Review extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    private Book book;
    @Setter
    private String content;
    @Setter
    private int rate;
    @OneToMany(mappedBy = "review", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<ReviewRecommend> reviewRecommends = new ArrayList<>();
    @Setter
    private int likeCount;
    @Setter
    private int dislikeCount;

//    @Version
//    private Long version;

    public Review(String content, int rate, Member member, Book book) {
        this.content = content;
        this.rate = rate;
        this.member = member;
        this.book = book;
    }

    public void incLike(){
        this.likeCount++;
    }

    public void decLike(){
        this.likeCount--;
    }

    public void incDislike(){
        this.dislikeCount++;
    }

    public void decDislike(){
        this.dislikeCount--;
    }
}
