package com.back.domain.book.book.repository;

import com.back.domain.book.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer>, BookRepositoryCustom {

    // 간단한 조회는 Spring Data JPA 기본 메서드 활용
    Optional<Book> findByIsbn13(String isbn13);
}