package com.back.domain.book.book.dto;

import com.back.domain.bookmarks.constant.ReadState;
import com.back.domain.review.review.dto.ReviewResponseDto;
import com.back.global.dto.PageResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BookDetailDto {
    private int id;
    private String title;
    private String imageUrl;
    private String publisher;
    private String isbn13;
    private int totalPage;
    private LocalDateTime publishedDate;
    private float avgRate;
    private String categoryName;
    private List<String> authors;
    private ReadState readState;
    private PageResponseDto<ReviewResponseDto> reviews;
}
