package com.back.domain.bookmarks.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBookmark is a Querydsl query type for Bookmark
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBookmark extends EntityPathBase<Bookmark> {

    private static final long serialVersionUID = 1592679904L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBookmark bookmark = new QBookmark("bookmark");

    public final com.back.global.jpa.entity.QBaseEntity _super = new com.back.global.jpa.entity.QBaseEntity(this);

    public final com.back.domain.book.book.entity.QBook book;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    public final DateTimePath<java.time.LocalDateTime> endReadDate = createDateTime("endReadDate", java.time.LocalDateTime.class);

    //inherited
    public final NumberPath<Integer> id = _super.id;

    public final com.back.domain.member.member.entity.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final ListPath<com.back.domain.note.entity.Note, com.back.domain.note.entity.QNote> notes = this.<com.back.domain.note.entity.Note, com.back.domain.note.entity.QNote>createList("notes", com.back.domain.note.entity.Note.class, com.back.domain.note.entity.QNote.class, PathInits.DIRECT2);

    public final NumberPath<Integer> readPage = createNumber("readPage", Integer.class);

    public final EnumPath<com.back.domain.bookmarks.constant.ReadState> readState = createEnum("readState", com.back.domain.bookmarks.constant.ReadState.class);

    public final DateTimePath<java.time.LocalDateTime> startReadDate = createDateTime("startReadDate", java.time.LocalDateTime.class);

    public QBookmark(String variable) {
        this(Bookmark.class, forVariable(variable), INITS);
    }

    public QBookmark(Path<? extends Bookmark> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBookmark(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBookmark(PathMetadata metadata, PathInits inits) {
        this(Bookmark.class, metadata, inits);
    }

    public QBookmark(Class<? extends Bookmark> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.book = inits.isInitialized("book") ? new com.back.domain.book.book.entity.QBook(forProperty("book"), inits.get("book")) : null;
        this.member = inits.isInitialized("member") ? new com.back.domain.member.member.entity.QMember(forProperty("member")) : null;
    }

}

