package com.back.domain.bookmarks.repository

import com.back.domain.book.author.entity.QAuthor
import com.back.domain.book.book.entity.QBook
import com.back.domain.book.wrote.entity.QWrote
import com.back.domain.bookmarks.constant.ReadState
import com.back.domain.bookmarks.dto.ReadStateCount
import com.back.domain.bookmarks.entity.Bookmark
import com.back.domain.bookmarks.entity.QBookmark
import com.back.domain.member.member.entity.Member
import com.back.global.jpa.querydsl.QuerydslUtil
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.Tuple
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors


class BookmarkRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) : BookmarkRepositoryCustom {
    override fun search(
        member: Member,
        category: String?,
        state: String?,
        keyword: String?,
        pageable: Pageable
    ): Page<Bookmark> {
        val builder = conditions(member, category, state, keyword)
        val specifiers = QuerydslUtil.toOrderSpecifiers(pageable.getSort(), QBookmark.bookmark)
        val bookmarks = queryFactory
            .select(QBookmark.bookmark).distinct()
            .from(QBookmark.bookmark)
            .join(QBookmark.bookmark.book, QBook.book)
            .leftJoin(QBook.book.authors, QWrote.wrote)
            .leftJoin(QWrote.wrote.author, QAuthor.author)
            .where(builder)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize().toLong())
            .orderBy(*specifiers.toTypedArray())
            .fetch()

        val total = queryFactory
            .select(QBookmark.bookmark.countDistinct())
            .from(QBookmark.bookmark)
            .join(QBookmark.bookmark.book, QBook.book)
            .leftJoin(QBook.book.authors, QWrote.wrote)
            .leftJoin(QWrote.wrote.author, QAuthor.author)
            .where(builder)

        return PageableExecutionUtils.getPage(bookmarks, pageable) { total.fetchOne()?: 0L }
    }

    override fun countReadState(
        member: Member,
        category: String?,
        state: String?,
        keyword: String?
    ): ReadStateCount {
        val builder = conditions(member, category, state, keyword)
        val counts = queryFactory!!
            .select(QBookmark.bookmark.readState, QBookmark.bookmark.countDistinct())
            .from(QBookmark.bookmark)
            .join(QBookmark.bookmark.book, QBook.book)
            .leftJoin(QBook.book.authors, QWrote.wrote)
            .leftJoin(QWrote.wrote.author, QAuthor.author)
            .where(builder)
            .groupBy(QBookmark.bookmark.readState)
            .fetch()
        val countMap = counts.stream().collect(
            Collectors.toMap(
                Function { tuple: Tuple? -> tuple!!.get(0, ReadState::class.java) },
                Function { tuple: Tuple? -> tuple!!.get<Long?>(1, Long::class.java) }
            ))

        val readStateCount = ReadStateCount(
            countMap.getOrDefault(ReadState.READ, 0L)?:0L,
            countMap.getOrDefault(ReadState.READING, 0L)?:0L,
            countMap.getOrDefault(ReadState.WISH, 0L)?:0L
        )
        return readStateCount
    }



    private fun conditions(member: Member, category: String?, readState: String?, keyword: String?): BooleanBuilder {
        val builder = BooleanBuilder()
        builder.and(QBookmark.bookmark.member.eq(member))

        if (category != null && !category.isBlank()) {
            builder.and(QBookmark.bookmark.book.category.name.eq(category))
        }

        if (readState != null && !readState.isBlank()) {
            builder.and(QBookmark.bookmark.readState.eq(ReadState.valueOf(readState.uppercase(Locale.getDefault()))))
        }

        if (keyword != null && !keyword.isBlank()) {
            builder.and(
                QBook.book.title.containsIgnoreCase(keyword)
                    .or(QAuthor.author.name.containsIgnoreCase(keyword))
            )
        }

        return builder
    }
}
