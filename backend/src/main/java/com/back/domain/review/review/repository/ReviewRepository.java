package com.back.domain.review.review.repository;

import com.back.domain.book.book.entity.Book;
import com.back.domain.member.member.entity.Member;
import com.back.domain.review.review.entity.Review;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    Optional<Review> findFirstByOrderByIdDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Review> findById(int id);

    Optional<Review> findByBookAndMember(Book book, Member member);

    @Query("SELECT AVG(rate) FROM Review WHERE member= :member")
    Optional<Double> findAverageRatingByMember(@Param("member" ) Member member);

    List<Review> findAllByMember(Member member);

    Page<Review> findByBookOrderByCreateDateDesc(Book book, Pageable pageable);

    Page<Review> findByBook(Book book, Pageable pageable);
}
