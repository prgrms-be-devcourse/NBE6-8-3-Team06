package com.back.domain.book.wrote.entity;

import com.back.domain.book.author.entity.Author;
import com.back.domain.book.book.entity.Book;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Wrote extends BaseEntity {
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    public Wrote(Author author, Book book) {
        this.author = author;
        this.book = book;
    }
}
