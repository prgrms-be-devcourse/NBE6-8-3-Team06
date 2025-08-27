package com.back.domain.bookmarks.entity;

import com.back.domain.book.book.entity.Book;
import com.back.domain.bookmarks.constant.ReadState;
import com.back.domain.member.member.entity.Member;
import com.back.domain.note.entity.Note;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Bookmark extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private ReadState readState;
    private int readPage;
    private LocalDateTime startReadDate;
    private LocalDateTime endReadDate;

    public Bookmark(Book book,  Member member) {
        this.book = book;
        this.member = member;
        this.readState = ReadState.WISH;
    }

    public void updateReadState(ReadState readState) {
        if(readState == ReadState.WISH){
            this.startReadDate = null;
            this.endReadDate = null;
            this.readPage = 0;
        }
        if(readState == ReadState.READING){
            this.endReadDate = null;
        }
        this.readState = readState;
    }

    public void updateReadPage(int readPage) {
        this.readPage = readPage;
    }
    public void updateStartReadDate(LocalDateTime startReadDate) {
        this.startReadDate = startReadDate;
    }
    public void updateEndReadDate(LocalDateTime endReadDate) {
        this.endReadDate = endReadDate;
    }

    @OneToMany(mappedBy = "bookmark", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private List<Note> notes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public int calculateReadingRate(){
        if(readState == ReadState.WISH) return 0;
        int totalPage = book.getTotalPage();
        if(totalPage == 0) return 0;
        if(readPage >= totalPage) return 100;
        if(readPage <= 0) return 0;
        double rate = ((double) readPage/totalPage) * 100;
        return (int) Math.round(rate);
    }

    public long calculateReadingDuration(){
        if(readState == ReadState.WISH) return 0;
        LocalDateTime effectiveEnd = (endReadDate == null) ? LocalDateTime.now() : endReadDate;
        return ChronoUnit.DAYS.between(startReadDate, effectiveEnd)+1;
    }
}
