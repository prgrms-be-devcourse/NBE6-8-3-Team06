package com.back.domain.review.review.repository

import com.back.domain.book.book.entity.Book
import com.back.domain.member.member.entity.Member
import com.back.domain.review.review.entity.QReview
import com.back.domain.review.review.entity.Review
import com.back.domain.review.reviewReport.entity.ReviewReportState
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import java.time.LocalDateTime

class ReviewRepositoryImpl(
    private val queryFactory: JPAQueryFactory
): ReviewRepositoryCustom {
    override fun findFirstByOrderByIdDesc(): Review? {
        val review = queryFactory
            .selectFrom(QReview.review)
            .where(QReview.review.deleted.eq(false))
            .orderBy(QReview.review.id.desc())
            .fetchFirst()
        return review
    }

    override fun findByBookAndMember(
        book: Book,
        member: Member
    ): Review? {
        val booleanBuilder = BooleanBuilder()
        booleanBuilder.and(QReview.review.book.eq(book))
        booleanBuilder.and(QReview.review.member.eq(member))
        booleanBuilder.and(QReview.review.deleted.eq(false))
        val review = queryFactory
            .selectFrom(QReview.review)
            .where(booleanBuilder)
            .fetchOne()
        return review
    }

    override fun findAverageRatingByMember(member: Member): Double {
        val booleanBuilder = BooleanBuilder()
        booleanBuilder.and(QReview.review.member.eq(member))
        booleanBuilder.and(QReview.review.deleted.eq(false))
        val avg = queryFactory
            .select(QReview.review.rate.avg())
            .from(QReview.review)
            .where(booleanBuilder)
            .fetchOne()
        return avg?:0.0
    }

    override fun findAllByMember(member: Member): MutableList<Review> {
        val booleanBuilder = BooleanBuilder()
        booleanBuilder.and(QReview.review.member.eq(member))
        booleanBuilder.and(QReview.review.deleted.eq(false))
        val reviews = queryFactory
            .selectFrom(QReview.review)
            .where(booleanBuilder)
            .fetch()
        return reviews
    }

    override fun findByBookOrderByCreateDateDesc(
        book: Book,
        pageable: Pageable
    ): Page<Review> {
        val booleanBuilder = BooleanBuilder()
        booleanBuilder.and(QReview.review.book.eq(book))
        booleanBuilder.and(QReview.review.deleted.eq(false))
        booleanBuilder.and(QReview.review.reportState.ne(ReviewReportState.EDIT_REQUIRED))
        val reviewPage = queryFactory
            .selectFrom(QReview.review)
            .where(booleanBuilder)
            .orderBy(QReview.review.createDate.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
        val count = queryFactory
            .select(QReview.review.count())
            .from(QReview.review)
            .where(booleanBuilder)
        return PageableExecutionUtils.getPage(reviewPage, pageable) { count.fetchOne()?: 0L }
    }

    override fun hardDeleteByElapsedDays(days: Int) {
        val booleanBuilder = BooleanBuilder(QReview.review.modifyDate.before(LocalDateTime.now().minusDays(days.toLong())))
        booleanBuilder.and(QReview.review.deleted.eq(true))
        queryFactory.delete(QReview.review)
            .where(booleanBuilder)
            .execute()
        return
    }
}