package com.back.domain.book.book.repository;

import com.back.domain.book.book.entity.Book;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.back.domain.book.author.entity.QAuthor.author;
import static com.back.domain.book.book.entity.QBook.book;
import static com.back.domain.book.category.entity.QCategory.category;
import static com.back.domain.book.wrote.entity.QWrote.wrote;

@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Book> findByTitleOrAuthorContaining(String keyword) {
        return queryFactory
                .selectFrom(book)
                .distinct()
                .leftJoin(book.authors, wrote)
                .leftJoin(wrote.author, author)
                .where(titleContains(keyword).or(authorNameContains(keyword)))
                .fetch();
    }

    @Override
    public List<Book> findValidBooksByTitleOrAuthorContaining(String query) {
        return queryFactory
                .selectFrom(book)
                .distinct()
                .leftJoin(book.authors, wrote)
                .leftJoin(wrote.author, author)
                .where(
                        isValidBook()
                                .and(titleContains(query).or(authorNameContains(query)))
                )
                .fetch();
    }

    @Override
    public Page<Book> findValidBooksByTitleOrAuthorContainingWithPaging(String query, Pageable pageable) {
        // 정렬을 포함한 쿼리
        List<Book> content = queryFactory
                .selectFrom(book)
                .distinct()
                .leftJoin(book.authors, wrote).fetchJoin()
                .leftJoin(wrote.author, author).fetchJoin()
                .leftJoin(book.category, category).fetchJoin()
                .where(
                        isValidBook()
                                .and(titleContains(query).or(authorNameContains(query)))
                )
                .orderBy(getSortOrder(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회
        Long total = queryFactory
                .select(book.countDistinct())
                .from(book)
                .leftJoin(book.authors, wrote)
                .leftJoin(wrote.author, author)
                .where(
                        isValidBook()
                                .and(titleContains(query).or(authorNameContains(query)))
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<Book> findAllValidBooks(Pageable pageable) {
        List<Book> content = queryFactory
                .selectFrom(book)
                .leftJoin(book.authors, wrote).fetchJoin()
                .leftJoin(wrote.author, author).fetchJoin()
                .leftJoin(book.category, category).fetchJoin()
                .where(isValidBook())
                .orderBy(getSortOrder(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(book.count())
                .from(book)
                .where(isValidBook())
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Optional<Book> findValidBookByIsbn13(String isbn13) {
        Book result = queryFactory
                .selectFrom(book)
                .leftJoin(book.authors, wrote).fetchJoin()
                .leftJoin(wrote.author, author).fetchJoin()
                .leftJoin(book.category, category).fetchJoin()
                .where(
                        book.isbn13.eq(isbn13)
                                .and(isValidBook())
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<Book> findValidBooksByCategory(String categoryName, Pageable pageable) {
        List<Book> content = queryFactory
                .selectFrom(book)
                .leftJoin(book.authors, wrote).fetchJoin()
                .leftJoin(wrote.author, author).fetchJoin()
                .leftJoin(book.category, category).fetchJoin()
                .where(
                        book.category.name.eq(categoryName)
                                .and(isValidBook())
                )
                .orderBy(getSortOrder(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(book.count())
                .from(book)
                .where(
                        book.category.name.eq(categoryName)
                                .and(isValidBook())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<Book> findValidBooksByQueryAndCategory(String query, String categoryName, Pageable pageable) {
        List<Book> content = queryFactory
                .selectFrom(book)
                .distinct()
                .leftJoin(book.authors, wrote).fetchJoin()
                .leftJoin(wrote.author, author).fetchJoin()
                .leftJoin(book.category, category).fetchJoin()
                .where(
                        isValidBook()
                                .and(book.category.name.eq(categoryName))
                                .and(titleContains(query).or(authorNameContains(query)))
                )
                .orderBy(getSortOrder(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(book.countDistinct())
                .from(book)
                .leftJoin(book.authors, wrote)
                .leftJoin(wrote.author, author)
                .where(
                        isValidBook()
                                .and(book.category.name.eq(categoryName))
                                .and(titleContains(query).or(authorNameContains(query)))
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /**
     * 동적 정렬을 위한 OrderSpecifier 생성
     */
    private OrderSpecifier<?>[] getSortOrder(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (sort != null && !sort.isEmpty()) {
            for (Sort.Order order : sort) {
                String property = order.getProperty();
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;

                switch (property.toLowerCase()) {
                    case "id":
                        orders.add(new OrderSpecifier<>(direction, book.id));
                        break;
                    case "title":
                        orders.add(new OrderSpecifier<>(direction, book.title));
                        break;
                    case "publisher":
                        orders.add(new OrderSpecifier<>(direction, book.publisher));
                        break;
                    case "publisheddate":
                        orders.add(new OrderSpecifier<>(direction, book.publishedDate));
                        break;
                    case "avgrate":
                        orders.add(new OrderSpecifier<>(direction, book.avgRate));
                        break;
                    case "author":
                        // 작가명으로 정렬 (첫 번째 작가 기준)
                        orders.add(new OrderSpecifier<>(direction, author.name));
                        break;
                    case "categoryname":
                        orders.add(new OrderSpecifier<>(direction, book.category.name));
                        break;
                    case "totalpage":
                        orders.add(new OrderSpecifier<>(direction, book.totalPage));
                        break;
                    default:
                        // 알 수 없는 정렬 필드는 ID로 대체
                        orders.add(new OrderSpecifier<>(direction, book.id));
                        break;
                }
            }
        }

        // 기본 정렬이 없으면 ID 내림차순으로 설정
        if (orders.isEmpty()) {
            orders.add(new OrderSpecifier<>(Order.DESC, book.id));
        }

        return orders.toArray(new OrderSpecifier[0]);
    }

    // 공통 조건 메서드들
    private BooleanExpression isValidBook() {
        return book.totalPage.gt(0);
    }

    private BooleanExpression titleContains(String keyword) {
        return keyword != null ? book.title.toLowerCase().contains(keyword.toLowerCase()) : null;
    }

    private BooleanExpression authorNameContains(String keyword) {
        return keyword != null ? author.name.toLowerCase().contains(keyword.toLowerCase()) : null;
    }
}