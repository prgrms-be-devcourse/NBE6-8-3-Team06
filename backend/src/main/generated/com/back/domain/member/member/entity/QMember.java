package com.back.domain.member.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = 826426997L;

    public static final QMember member = new QMember("member1");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    public final ListPath<com.back.domain.bookmarks.entity.Bookmark, com.back.domain.bookmarks.entity.QBookmark> bookmarks = this.<com.back.domain.bookmarks.entity.Bookmark, com.back.domain.bookmarks.entity.QBookmark>createList("bookmarks", com.back.domain.bookmarks.entity.Bookmark.class, com.back.domain.bookmarks.entity.QBookmark.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    public final StringPath email = createString("email");

    //inherited
    public final NumberPath<Integer> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final StringPath name = createString("name");

    public final ListPath<com.back.domain.note.entity.Note, com.back.domain.note.entity.QNote> notes = this.<com.back.domain.note.entity.Note, com.back.domain.note.entity.QNote>createList("notes", com.back.domain.note.entity.Note.class, com.back.domain.note.entity.QNote.class, PathInits.DIRECT2);

    public final StringPath password = createString("password");

    public final StringPath refreshToken = createString("refreshToken");

    public final ListPath<com.back.domain.review.review.entity.Review, com.back.domain.review.review.entity.QReview> reviews = this.<com.back.domain.review.review.entity.Review, com.back.domain.review.review.entity.QReview>createList("reviews", com.back.domain.review.review.entity.Review.class, com.back.domain.review.review.entity.QReview.class, PathInits.DIRECT2);

    public QMember(String variable) {
        super(Member.class, forVariable(variable));
    }

    public QMember(Path<? extends Member> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMember(PathMetadata metadata) {
        super(Member.class, metadata);
    }

}

