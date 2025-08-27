package com.back.domain.review.review.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReview is a Querydsl query type for Review
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReview extends EntityPathBase<Review> {

    private static final long serialVersionUID = -403271309L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReview review = new QReview("review");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    public final com.back.domain.book.book.entity.QBook book;

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    public final NumberPath<Integer> dislikeCount = createNumber("dislikeCount", Integer.class);

    //inherited
    public final NumberPath<Integer> id = _super.id;

    public final NumberPath<Integer> likeCount = createNumber("likeCount", Integer.class);

    public final com.back.domain.member.member.entity.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final NumberPath<Integer> rate = createNumber("rate", Integer.class);

    public final ListPath<com.back.domain.review.reviewRecommend.entity.ReviewRecommend, com.back.domain.review.reviewRecommend.entity.QReviewRecommend> reviewRecommends = this.<com.back.domain.review.reviewRecommend.entity.ReviewRecommend, com.back.domain.review.reviewRecommend.entity.QReviewRecommend>createList("reviewRecommends", com.back.domain.review.reviewRecommend.entity.ReviewRecommend.class, com.back.domain.review.reviewRecommend.entity.QReviewRecommend.class, PathInits.DIRECT2);

    public QReview(String variable) {
        this(Review.class, forVariable(variable), INITS);
    }

    public QReview(Path<? extends Review> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReview(PathMetadata metadata, PathInits inits) {
        this(Review.class, metadata, inits);
    }

    public QReview(Class<? extends Review> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.book = inits.isInitialized("book") ? new com.back.domain.book.book.entity.QBook(forProperty("book"), inits.get("book")) : null;
        this.member = inits.isInitialized("member") ? new com.back.domain.member.member.entity.QMember(forProperty("member")) : null;
    }

}

