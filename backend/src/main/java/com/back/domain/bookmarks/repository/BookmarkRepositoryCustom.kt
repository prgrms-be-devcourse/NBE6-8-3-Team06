package com.back.domain.bookmarks.repository;

import com.back.domain.bookmarks.dto.ReadStateCount;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.member.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookmarkRepositoryCustom {
    Page<Bookmark> search(Member member, String category, String state, String keyword, Pageable pageable);
    ReadStateCount countReadState(Member member, String category, String state, String keyword);
}
