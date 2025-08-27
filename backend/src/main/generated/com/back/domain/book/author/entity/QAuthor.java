package com.back.domain.book.author.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAuthor is a Querydsl query type for Author
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAuthor extends EntityPathBase<Author> {

    private static final long serialVersionUID = -2034248280L;

    public static final QAuthor author = new QAuthor("author");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    public final ListPath<com.back.domain.book.wrote.entity.Wrote, com.back.domain.book.wrote.entity.QWrote> books = this.<com.back.domain.book.wrote.entity.Wrote, com.back.domain.book.wrote.entity.QWrote>createList("books", com.back.domain.book.wrote.entity.Wrote.class, com.back.domain.book.wrote.entity.QWrote.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    //inherited
    public final NumberPath<Integer> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final StringPath name = createString("name");

    public QAuthor(String variable) {
        super(Author.class, forVariable(variable));
    }

    public QAuthor(Path<? extends Author> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAuthor(PathMetadata metadata) {
        super(Author.class, metadata);
    }

}

