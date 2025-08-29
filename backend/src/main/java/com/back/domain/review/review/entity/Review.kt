package com.back.domain.review.review.entity

import com.back.domain.book.book.entity.Book
import com.back.domain.member.member.entity.Member
import com.back.domain.review.reviewRecommend.entity.ReviewRecommend
import com.back.global.jpa.entity.BaseEntity
import jakarta.persistence.*
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

@Entity
class Review //    @Version
//    private Long version;
    (
    var content: String,
    var rate: Int,
    var spoiler: Boolean,
    @field:ManyToOne(fetch = FetchType.LAZY)
    var member: Member,
    @field:ManyToOne(fetch = FetchType.LAZY)
    var book: Book
) : BaseEntity() {
    @OneToMany(mappedBy = "review", fetch = FetchType.LAZY, orphanRemoval = true, cascade = [CascadeType.ALL])
    private val reviewRecommends: MutableList<ReviewRecommend> = ArrayList()

    var likeCount = 0


    var dislikeCount = 0

    fun incLike() {
        this.likeCount++
    }

    fun decLike() {
        this.likeCount--
    }

    fun incDislike() {
        this.dislikeCount++
    }

    fun decDislike() {
        this.dislikeCount--
    }
}
