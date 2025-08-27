package com.back.domain.book.book.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBook is a Querydsl query type for Book
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBook extends EntityPathBase<Book> {

    private static final long serialVersionUID = 1071695844L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBook book = new QBook("book");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    public final ListPath<com.back.domain.book.wrote.entity.Wrote, com.back.domain.book.wrote.entity.QWrote> authors = this.<com.back.domain.book.wrote.entity.Wrote, com.back.domain.book.wrote.entity.QWrote>createList("authors", com.back.domain.book.wrote.entity.Wrote.class, com.back.domain.book.wrote.entity.QWrote.class, PathInits.DIRECT2);

    public final NumberPath<Float> avgRate = createNumber("avgRate", Float.class);

    public final ListPath<com.back.domain.bookmarks.entity.Bookmark, com.back.domain.bookmarks.entity.QBookmark> bookmarks = this.<com.back.domain.bookmarks.entity.Bookmark, com.back.domain.bookmarks.entity.QBookmark>createList("bookmarks", com.back.domain.bookmarks.entity.Bookmark.class, com.back.domain.bookmarks.entity.QBookmark.class, PathInits.DIRECT2);

    public final com.back.domain.book.category.entity.QCategory category;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    //inherited
    public final NumberPath<Integer> id = _super.id;

    public final StringPath imageUrl = createString("imageUrl");

    public final StringPath isbn13 = createString("isbn13");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final DateTimePath<java.time.LocalDateTime> publishedDate = createDateTime("publishedDate", java.time.LocalDateTime.class);

    public final StringPath publisher = createString("publisher");

    public final ListPath<com.back.domain.review.review.entity.Review, com.back.domain.review.review.entity.QReview> reviews = this.<com.back.domain.review.review.entity.Review, com.back.domain.review.review.entity.QReview>createList("reviews", com.back.domain.review.review.entity.Review.class, com.back.domain.review.review.entity.QReview.class, PathInits.DIRECT2);

    public final StringPath title = createString("title");

    public final NumberPath<Integer> totalPage = createNumber("totalPage", Integer.class);

    public QBook(String variable) {
        this(Book.class, forVariable(variable), INITS);
    }

    public QBook(Path<? extends Book> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBook(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBook(PathMetadata metadata, PathInits inits) {
        this(Book.class, metadata, inits);
    }

    public QBook(Class<? extends Book> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new com.back.domain.book.category.entity.QCategory(forProperty("category")) : null;
    }

}

