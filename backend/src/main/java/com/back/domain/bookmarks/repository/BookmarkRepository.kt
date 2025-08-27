package com.back.domain.bookmarks.repository;

import com.back.domain.book.book.entity.Book;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.member.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Integer>, JpaSpecificationExecutor<Bookmark>, BookmarkRepositoryCustom {
    Optional<Bookmark> findById(int id);
    Optional<Bookmark> findByBook(Book book);
    Optional<Bookmark> findByIdAndMember(int id, Member member);
    List<Bookmark> findByMember(Member member);
    Optional<Bookmark> findByMemberAndBook(Member member, Book book);
    @Query("SELECT b FROM Bookmark b WHERE b.member = :member AND b.book.id IN :bookIds")
    List<Bookmark> findByMemberAndBookIds(@Param("member") Member member, @Param("bookIds") List<Integer> bookIds);
    @Query("SELECT b FROM Bookmark b WHERE b.member = :member AND b.book = :book")
    Optional<Bookmark> findByMemberAndBookWithFresh(@Param("member") Member member, @Param("book") Book book);
    boolean existsByMemberAndBook(Member member, Book book);
    Optional<Bookmark> getBookmarkByMemberOrderByIdDesc(Member member);
}
