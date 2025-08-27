package com.back.domain.review.reviewRecommend.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReviewRecommend is a Querydsl query type for ReviewRecommend
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReviewRecommend extends EntityPathBase<ReviewRecommend> {

    private static final long serialVersionUID = -2039055063L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReviewRecommend reviewRecommend = new QReviewRecommend("reviewRecommend");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    //inherited
    public final NumberPath<Integer> id = _super.id;

    public final BooleanPath isRecommended = createBoolean("isRecommended");

    public final com.back.domain.member.member.entity.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final com.back.domain.review.review.entity.QReview review;

    public QReviewRecommend(String variable) {
        this(ReviewRecommend.class, forVariable(variable), INITS);
    }

    public QReviewRecommend(Path<? extends ReviewRecommend> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReviewRecommend(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReviewRecommend(PathMetadata metadata, PathInits inits) {
        this(ReviewRecommend.class, metadata, inits);
    }

    public QReviewRecommend(Class<? extends ReviewRecommend> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.back.domain.member.member.entity.QMember(forProperty("member")) : null;
        this.review = inits.isInitialized("review") ? new com.back.domain.review.review.entity.QReview(forProperty("review"), inits.get("review")) : null;
    }

}

