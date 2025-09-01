package com.back.domain.review.reviewReport.repository

import com.back.domain.review.reviewReport.entity.ReviewReport
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ReviewReportRepositoryCustom {

    fun search(keyword: String?, pageable: Pageable): Page<ReviewReport>
}