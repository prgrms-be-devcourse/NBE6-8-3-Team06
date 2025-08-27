package com.back.domain.book.book.entity;

import com.back.domain.book.category.entity.Category;
import com.back.domain.book.wrote.entity.Wrote;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.review.review.entity.Review;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Book extends BaseEntity {
    @Column(nullable = false)
    String title;
    String imageUrl;
    String publisher;
    int totalPage;
    LocalDateTime publishedDate;
    float avgRate = 0.0f;

    @Column(unique = true)
    String isbn13;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Wrote> authors = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "book", orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bookmark> bookmarks = new ArrayList<>();

    public Book(String title, String publisher, Category category){
        this.title = title;
        this.publisher = publisher;
        this.category = category;
    }

}
