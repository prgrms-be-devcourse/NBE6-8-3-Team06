package com.back.domain.book.book.repository

import com.back.domain.book.author.entity.QAuthor
import com.back.domain.book.book.entity.Book
import com.back.domain.book.book.entity.QBook
import com.back.domain.book.category.entity.QCategory
import com.back.domain.book.wrote.entity.QWrote
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

@Repository
class BookRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : BookRepositoryCustom {

    private val book = QBook.book
    private val wrote = QWrote.wrote
    private val author = QAuthor.author
    private val category = QCategory.category

    override fun findByTitleOrAuthorContaining(keyword: String): List<Book> {
        return queryFactory
            .selectFrom(book)
            .distinct()
            .leftJoin(book._authors, wrote)
            .leftJoin(wrote.author, author)
            .where(titleContains(keyword).or(authorNameContains(keyword)))
            .fetch()
    }

    override fun findValidBooksByTitleOrAuthorContaining(query: String): List<Book> {
        return queryFactory
            .selectFrom(book)
            .distinct()
            .leftJoin(book._authors, wrote)
            .leftJoin(wrote.author, author)
            .where(isValidBook.and(titleContains(query).or(authorNameContains(query))))
            .fetch()
    }

    override fun findValidBooksByTitleOrAuthorContainingWithPaging(
        query: String,
        pageable: Pageable
    ): Page<Book> {
        // 정렬을 포함한 쿼리
        val content = queryFactory
            .selectFrom(book)
            .distinct()
            .leftJoin(book._authors, wrote).fetchJoin()
            .leftJoin(wrote.author, author).fetchJoin()
            .leftJoin(book.category, category).fetchJoin()
            .where(isValidBook.and(titleContains(query).or(authorNameContains(query))))
            .orderBy(*getSortOrder(pageable.sort))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        // 전체 개수 조회
        val total = queryFactory
            .select(book.countDistinct())
            .from(book)
            .leftJoin(book._authors, wrote)
            .leftJoin(wrote.author, author)
            .where(isValidBook.and(titleContains(query).or(authorNameContains(query))))
            .fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }

    override fun findAllValidBooks(pageable: Pageable): Page<Book> {
        val content = queryFactory
            .selectFrom(book)
            .leftJoin(book._authors, wrote).fetchJoin()
            .leftJoin(wrote.author, author).fetchJoin()
            .leftJoin(book.category, category).fetchJoin()
            .where(isValidBook)
            .orderBy(*getSortOrder(pageable.sort))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val total = queryFactory
            .select(book.count())
            .from(book)
            .where(isValidBook)
            .fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }

    override fun findValidBookByIsbn13(isbn13: String): Book? {
        return queryFactory
            .selectFrom(book)
            .leftJoin(book._authors, wrote).fetchJoin()
            .leftJoin(wrote.author, author).fetchJoin()
            .leftJoin(book.category, category).fetchJoin()
            .where(book.isbn13.eq(isbn13).and(isValidBook))
            .fetchOne()
    }

    override fun findValidBooksByCategory(categoryName: String, pageable: Pageable): Page<Book> {
        val content = queryFactory
            .selectFrom(book)
            .leftJoin(book._authors, wrote).fetchJoin()
            .leftJoin(wrote.author, author).fetchJoin()
            .leftJoin(book.category, category).fetchJoin()
            .where(book.category.name.eq(categoryName).and(isValidBook))
            .orderBy(*getSortOrder(pageable.sort))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val total = queryFactory
            .select(book.count())
            .from(book)
            .where(book.category.name.eq(categoryName).and(isValidBook))
            .fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }

    override fun findValidBooksByQueryAndCategory(
        query: String,
        categoryName: String,
        pageable: Pageable
    ): Page<Book> {
        val content = queryFactory
            .selectFrom(book)
            .distinct()
            .leftJoin(book._authors, wrote).fetchJoin()
            .leftJoin(wrote.author, author).fetchJoin()
            .leftJoin(book.category, category).fetchJoin()
            .where(
                isValidBook
                    .and(book.category.name.eq(categoryName))
                    .and(titleContains(query).or(authorNameContains(query)))
            )
            .orderBy(*getSortOrder(pageable.sort))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val total = queryFactory
            .select(book.countDistinct())
            .from(book)
            .leftJoin(book._authors, wrote)
            .leftJoin(wrote.author, author)
            .where(
                isValidBook
                    .and(book.category.name.eq(categoryName))
                    .and(titleContains(query).or(authorNameContains(query)))
            )
            .fetchOne() ?: 0L

        return PageImpl(content, pageable, total)
    }

    /**
     * 동적 정렬을 위한 OrderSpecifier 생성
     */
    private fun getSortOrder(sort: Sort): Array<OrderSpecifier<*>> {
        val orders = mutableListOf<OrderSpecifier<*>>()

        if (sort.isSorted) {
            sort.forEach { order ->
                val property = order.property
                val direction = if (order.isAscending) Order.ASC else Order.DESC

                when (property.lowercase()) {
                    "id" -> orders.add(OrderSpecifier(direction, book.id))
                    "title" -> orders.add(OrderSpecifier(direction, book.title))
                    "publisher" -> orders.add(OrderSpecifier(direction, book.publisher))
                    "publisheddate" -> orders.add(OrderSpecifier(direction, book.publishedDate))
                    "avgrate" -> orders.add(OrderSpecifier(direction, book.avgRate))
                    "author" -> orders.add(OrderSpecifier(direction, author.name))
                    "categoryname" -> orders.add(OrderSpecifier(direction, book.category.name))
                    "totalpage" -> orders.add(OrderSpecifier(direction, book.totalPage))
                    else -> orders.add(OrderSpecifier(direction, book.id)) // 알 수 없는 정렬 필드는 ID로 대체
                }
            }
        }

        // 기본 정렬이 없으면 ID 내림차순으로 설정
        if (orders.isEmpty()) {
            orders.add(OrderSpecifier(Order.DESC, book.id))
        }

        return orders.toTypedArray()
    }

    // 공통 조건 메서드들
    private val isValidBook: BooleanExpression
        get() = book.totalPage.gt(0)

    private fun titleContains(keyword: String): BooleanExpression {
        return book.title.toLowerCase().contains(keyword.lowercase())
    }

    private fun authorNameContains(keyword: String): BooleanExpression {
        return author.name.toLowerCase().contains(keyword.lowercase())
    }
}