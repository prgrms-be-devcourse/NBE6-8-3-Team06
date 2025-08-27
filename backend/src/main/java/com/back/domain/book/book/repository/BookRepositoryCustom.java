package com.back.domain.book.book.repository;

import com.back.domain.book.book.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BookRepositoryCustom {

    List<Book> findByTitleOrAuthorContaining(String keyword);

    List<Book> findValidBooksByTitleOrAuthorContaining(String query);

    Page<Book> findValidBooksByTitleOrAuthorContainingWithPaging(String query, Pageable pageable);

    Page<Book> findAllValidBooks(Pageable pageable);

    Optional<Book> findValidBookByIsbn13(String isbn13);

    Page<Book> findValidBooksByCategory(String categoryName, Pageable pageable);

    Page<Book> findValidBooksByQueryAndCategory(String query, String categoryName, Pageable pageable);
}