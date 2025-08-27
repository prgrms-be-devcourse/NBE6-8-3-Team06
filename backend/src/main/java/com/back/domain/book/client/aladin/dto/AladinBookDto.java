package com.back.domain.book.client.aladin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AladinBookDto {
    private String title;
    private String imageUrl;
    private String publisher;
    private String isbn13;
    private int totalPage;
    private LocalDateTime publishedDate;
    private String categoryName;
    private String mallType;
    private List<String> authors;
}