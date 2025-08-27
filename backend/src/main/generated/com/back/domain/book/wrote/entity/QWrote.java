package com.back.domain.book.wrote.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWrote is a Querydsl query type for Wrote
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWrote extends EntityPathBase<Wrote> {

    private static final long serialVersionUID = 3283288L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWrote wrote = new QWrote("wrote");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    public final com.back.domain.book.author.entity.QAuthor author;

    public final com.back.domain.book.book.entity.QBook book;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    //inherited
    public final NumberPath<Integer> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public QWrote(String variable) {
        this(Wrote.class, forVariable(variable), INITS);
    }

    public QWrote(Path<? extends Wrote> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWrote(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWrote(PathMetadata metadata, PathInits inits) {
        this(Wrote.class, metadata, inits);
    }

    public QWrote(Class<? extends Wrote> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.author = inits.isInitialized("author") ? new com.back.domain.book.author.entity.QAuthor(forProperty("author")) : null;
        this.book = inits.isInitialized("book") ? new com.back.domain.book.book.entity.QBook(forProperty("book"), inits.get("book")) : null;
    }

}

