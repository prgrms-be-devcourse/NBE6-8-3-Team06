package com.back.domain.bookmarks.repository;

import com.back.domain.bookmarks.constant.ReadState;
import com.back.domain.bookmarks.dto.ReadStateCount;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.member.member.entity.Member;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.back.domain.book.author.entity.QAuthor.author;
import static com.back.domain.book.book.entity.QBook.book;
import static com.back.domain.book.wrote.entity.QWrote.wrote;
import static com.back.domain.bookmarks.entity.QBookmark.bookmark;

@RequiredArgsConstructor
public class BookmarkRepositoryCustomImpl implements BookmarkRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Bookmark> search(Member member, String category, String readState, String keyword, Pageable pageable) {
        BooleanBuilder builder = conditions(member, category, readState, keyword);

        List<Bookmark> bookmarks = queryFactory
                .select(bookmark).distinct()
                .from(bookmark)
                .join(bookmark.book, book)
                .leftJoin(book.authors, wrote)
                .leftJoin(wrote.author, author)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .fetch();

        Long total = queryFactory
                .select(bookmark.countDistinct())
                .from(bookmark)
                .join(bookmark.book, book)
                .leftJoin(book.authors, wrote)
                .leftJoin(wrote.author, author)
                .where(builder)
                .fetchOne();
        return new PageImpl<>(bookmarks, pageable, total != null ? total : 0L);
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort){
        if(sort.isEmpty()){
            return new OrderSpecifier[]{bookmark.createDate.desc()};
        }
        return sort.stream().map(order -> {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            String property = order.getProperty();
            PathBuilder<Bookmark> pathBuilder = new PathBuilder<>(Bookmark.class, "bookmark");
            return new OrderSpecifier(direction, pathBuilder.get(property));
        })
                .toArray(OrderSpecifier[]::new);
    }

    @Override
    public ReadStateCount countReadState(Member member, String category, String readState, String keyword) {
        BooleanBuilder builder = conditions(member, category, readState, keyword);

        List<Tuple> counts = queryFactory
                .select(bookmark.readState, bookmark.countDistinct())
                .from(bookmark)
                .join(bookmark.book, book)
                .leftJoin(book.authors, wrote)
                .leftJoin(wrote.author, author)
                .where(builder)
                .groupBy(bookmark.readState)
                .fetch();

        Map<ReadState, Long> countMap = counts.stream().collect(Collectors.toMap(
                tuple -> tuple.get(0, ReadState.class),
                tuple -> tuple.get(1, Long.class)
        ));

        ReadStateCount readStateCount = new ReadStateCount(
                countMap.getOrDefault(ReadState.READ, 0L),
                countMap.getOrDefault(ReadState.READING, 0L),
                countMap.getOrDefault(ReadState.WISH, 0L)
        );
        return readStateCount;
    }

    private BooleanBuilder conditions(Member member, String category, String readState, String keyword) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(bookmark.member.eq(member));

        if(category != null && !category.isBlank()){
            builder.and(bookmark.book.category.name.eq(category));
        }

        if(readState != null && !readState.isBlank()){
            builder.and(bookmark.readState.eq(ReadState.valueOf(readState.toUpperCase())));
        }

        if(keyword != null && !keyword.isBlank()){
            builder.and(
                    book.title.containsIgnoreCase(keyword)
                            .or(author.name.containsIgnoreCase(keyword))
            );
        }

        return builder;
    }
}
