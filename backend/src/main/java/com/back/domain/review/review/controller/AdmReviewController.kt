package com.back.domain.review.review.controller

import com.back.domain.review.review.service.ReviewService
import com.back.global.rsData.RsData
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("/adm/review")
class AdmReviewController(
    private val reviewService: ReviewService,
) {

    @DeleteMapping("/hard")
    fun hardDeleteAll(
        @RequestParam("elapsedays", required = true) elapseDays:Int
    ): RsData<Void> {
        reviewService.hardDelete(elapseDays)
        return RsData("200-1", "Reviews deleted successfully")
    }

    @DeleteMapping("/soft/{review_id}")
    fun softDelete(
        @RequestParam("review_id") reviewId:Int,
    ):RsData<Void>{
        reviewService.softDelete(reviewId)
        return RsData("200-1", "Review deleted successfully")
    }

}