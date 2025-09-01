package com.back.domain.review.reviewReport.repository

import com.back.domain.review.reviewReport.entity.ReviewReport
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewReportRepository: JpaRepository<ReviewReport, Int>, ReviewReportRepositoryCustom {
}