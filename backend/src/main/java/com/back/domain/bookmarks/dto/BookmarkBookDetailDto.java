package com.back.domain.bookmarks.dto;

import com.back.domain.book.book.entity.Book;

import java.time.LocalDateTime;
import java.util.List;

public record BookmarkBookDetailDto(
        int id,
        String isbn13,
        String title,
        String imageUrl,
        String publisher,
        int totalPage,
        double avgRate,
        String category,
        List<String> authors,
        LocalDateTime publishDate
) {
    public BookmarkBookDetailDto(Book book){
        this(
                book.getId(),
                book.getIsbn13(),
                book.getTitle(),
                book.getImageUrl(),
                book.getPublisher(),
                book.getTotalPage(),
                book.getAvgRate(),
                book.getCategory().getName(),
                book.getAuthors() !=null ? book.getAuthors().stream().map(a -> a.getAuthor().getName()).toList() : List.of(),
                book.getPublishedDate()
        );
    }
}
