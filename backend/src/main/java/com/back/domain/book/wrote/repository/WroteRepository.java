package com.back.domain.book.wrote.repository;
import com.back.domain.book.author.entity.Author;
import com.back.domain.book.book.entity.Book;
import com.back.domain.book.wrote.entity.Wrote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WroteRepository extends JpaRepository<Wrote, Integer> {
    boolean existsByAuthorAndBook(Author author, Book book);
}