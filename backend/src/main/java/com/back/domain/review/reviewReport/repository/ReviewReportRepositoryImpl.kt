package com.back.domain.review.reviewReport.repository

import com.back.domain.review.reviewReport.entity.QReviewReport
import com.back.domain.review.reviewReport.entity.ReviewReport
import com.back.global.jpa.querydsl.QuerydslUtil
import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.web.client.HttpServerErrorException

class ReviewReportRepositoryImpl(
    private val queryFactory: JPAQueryFactory
): ReviewReportRepositoryCustom {

    override fun search(
        keyword: String?,
        pageable: Pageable,
        processed: Boolean
    ): Page<ReviewReport> {
        val specifiers = QuerydslUtil.toOrderSpecifiers(pageable.sort, QReviewReport.reviewReport)
        val booleanBuilder = QuerydslUtil.buildKeywordPredicate("reason", keyword, QReviewReport.reviewReport)?: BooleanBuilder()
        booleanBuilder.and(QReviewReport.reviewReport.processed.eq(processed))

        val data = queryFactory
            .selectFrom(QReviewReport.reviewReport)
            .where(booleanBuilder)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(*specifiers.toTypedArray())
            .fetch()

        val total = queryFactory
            .select(QReviewReport.reviewReport.count())
            .from(QReviewReport.reviewReport)
            .where(booleanBuilder)
        return PageableExecutionUtils.getPage(data, pageable) { total.fetchOne()?: 0L }
    }
}