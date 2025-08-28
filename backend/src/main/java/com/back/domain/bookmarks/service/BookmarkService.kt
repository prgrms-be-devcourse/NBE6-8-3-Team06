package com.back.domain.bookmarks.service

import com.back.domain.book.book.entity.Book
import com.back.domain.book.book.repository.BookRepository
import com.back.domain.bookmarks.constant.ReadState
import com.back.domain.bookmarks.dto.BookmarkDetailDto
import com.back.domain.bookmarks.dto.BookmarkDto
import com.back.domain.bookmarks.dto.BookmarkModifyResponseDto
import com.back.domain.bookmarks.dto.BookmarkReadStatesDto
import com.back.domain.bookmarks.entity.Bookmark
import com.back.domain.bookmarks.repository.BookmarkRepository
import com.back.domain.member.member.entity.Member
import com.back.domain.review.review.entity.Review
import com.back.domain.review.review.repository.ReviewRepository
import com.back.domain.review.review.service.ReviewService
import com.back.global.exception.ServiceException
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collectors

@Service
class BookmarkService(
    private val bookmarkRepository: BookmarkRepository,
            private val bookRepository: BookRepository,
            private val reviewService: ReviewService,
            private val reviewRepository: ReviewRepository,
) {


    /**
     * 책을 북마크에 추가
     * @param bookId
     * @param member
     * @return Bookmark Entity
     * @throws NoSuchElementException 책이 없는 경우
     * @throws IllegalStateException 이미 추가된 책인 경우
     */
    fun save(bookId: Int, member: Member): Bookmark {
        val book = bookRepository.findById(bookId)
            .orElseThrow(Supplier { NoSuchElementException("${bookId}번 등록된 책이 없습니다.") })
        check(!bookmarkRepository.existsByMemberAndBook(member, book)) { "이미 추가된 책입니다." }
        val bookmark = Bookmark(book, member)
        return bookmarkRepository.save<Bookmark>(bookmark)
    }

    /**
     * 사용자의 모든 북마크 목록 조회
     * @param member
     * @return BookmarkDto 리스트
     */
    fun toList(member: Member): MutableList<BookmarkDto> {
        val bookmarks: MutableList<Bookmark> = bookmarkRepository.findByMember(member)
        //리뷰 포함 리스트 반환
        return convertToBookmarkDtoList(bookmarks, member)
    }

    /**
     * 사용자의 북마크 목록을 페이징, 필터링하여 조회
     * @param member
     * @param page
     * @param size
     * @param sort : type,order
     * @param category
     * @param state
     * @param keyword
     * @return BookmarkDto 페이징
     */
    fun toPage(
        member: Member,
        pageable: Pageable,
        category: String?,
        state: String?,
        keyword: String?
    ): Page<BookmarkDto> {
        val bookmarks: Page<Bookmark> = bookmarkRepository.search(member, category, state, keyword, pageable)
        //리뷰 포함 리스트 반환
        val dtoList = convertToBookmarkDtoList(bookmarks.getContent(), member)
        return PageImpl(dtoList, pageable, bookmarks.getTotalElements())
    }

    /**
     * 북마크 상세 조회
     * @param member
     * @param bookmarkId
     * @return BookmarkDetailDto
     */
    fun getBookmarkById(member: Member, bookmarkId: Int): BookmarkDetailDto {
        val bookmark = bookmarkRepository.findByIdAndMember(bookmarkId, member)
            .orElseThrow(Supplier {
                NoSuchElementException(
                    "${bookmarkId}번 데이터가 없습니다."
                )
            })
        val review = getReview(bookmark)
        return BookmarkDetailDto(bookmark, review)
    }

    fun findById(bookmarkId: Int): Bookmark {
        return bookmarkRepository.findById(bookmarkId).orElseThrow(Supplier {
            NoSuchElementException(
                "${bookmarkId}번 데이터가 없습니다."
            )
        })
    }

    /**
     * 북마크 수정
     * @param member
     * @param bookmarkId
     * @param state
     * @param startReadDate
     * @param endReadDate
     * @param readPage
     * @return BookmarkModifyResponseDto
     */
    fun modifyBookmark(
        member: Member,
        bookmarkId: Int,
        state: String?,
        startReadDate: LocalDateTime?,
        endReadDate: LocalDateTime?,
        readPage: Int
    ): BookmarkModifyResponseDto {
        val bookmark = findByIdAndMember(bookmarkId, member)
        if (endReadDate != null) {
            bookmark.updateEndReadDate(endReadDate)
        }
        if (startReadDate != null) {
            bookmark.updateStartReadDate(startReadDate)
        }
        if (readPage > 0) {
            bookmark.updateReadPage(readPage)
        }
        if (state != null) {
            val readState = ReadState.valueOf(state.uppercase(Locale.getDefault()))
            bookmark.updateReadState(readState)
        }
        bookmarkRepository.flush()
        return BookmarkModifyResponseDto(bookmark)
    }

    /**
     * 북마크 삭제
     * @param member
     * @param bookmarkId
     */
    fun deleteBookmark(member: Member, bookmarkId: Int) {
        val bookmark = findByIdAndMember(bookmarkId, member)
        bookmarkRepository.delete(bookmark)
        //리뷰가 있는 경우, 리뷰 삭제
        if (getReview(bookmark) != null) {
            reviewService.deleteReview(bookmark.book, member)
        }
    }

    //북마크 권한 체크
    private fun findByIdAndMember(bookmarkId: Int, member: Member): Bookmark {
        return bookmarkRepository.findByIdAndMember(bookmarkId, member)
            .orElseThrow(Supplier { ServiceException("403-1", "해당 북마크에 대한 권한이 없습니다.") })
    }

    /**
     * 북마크 통계 조회 - 필터 조건
     * @param member
     * @param category
     * @param readState
     * @param keyword
     * @return BookmarkReadStatesDto
     */
    fun getReadStatesCount(
        member: Member,
        category: String?,
        readState: String?,
        keyword: String?
    ): BookmarkReadStatesDto {
        val readStateCount = bookmarkRepository.countReadState(member, category, readState, keyword)
        val totalCount = readStateCount.READ + readStateCount.READING + readStateCount.WISH
        var avgRate = 0.0
        //필터 조건이 있는 경우 리뷰 평점 조회하지 않음. 리뷰 조건은 전체 통계에서만 조회
        if (category == null && readState == null && keyword == null) {
            avgRate = getAvgRate(member)
        }
        return BookmarkReadStatesDto(
            totalCount, avgRate, readStateCount
        )
    }

    private fun getAvgRate(member: Member): Double {
        return reviewRepository.findAverageRatingByMember(member).orElse(0.0)
    }

    private fun getReview(bookmark: Bookmark): Review? {
        return reviewService.findByBookAndMember(bookmark.book, bookmark.member).orElse(null)
    }

    private fun getReadStateByMemberAndBook(member: Member, book: Book): ReadState? {
        val bookmark: Optional<Bookmark> = bookmarkRepository.findByMemberAndBookWithFresh(member, book)
        return bookmark.map(Bookmark::readState).orElse(null)
    }

    fun getReadStateByMemberAndBookId(member: Member, bookId: Int): ReadState? {
        val book = bookRepository.findById(bookId).orElse(null)
        if (book == null) {
            return null
        }
        return getReadStateByMemberAndBook(member, book)
    }

    fun getReadStatesForBooks(member: Member, bookIds: MutableList<Int>): MutableMap<Int, ReadState> {
        val bookmarks: MutableList<Bookmark> = bookmarkRepository.findByMember(member)

        return bookmarks.stream()
            .filter { bookmark: Bookmark -> bookIds.contains(bookmark.book.id) }
            .collect(
                Collectors.toMap(
                    Function { bookmark: Bookmark -> bookmark.book.id },
                    Bookmark::readState
                )
            )
    }

    /**
     * 북마크 리뷰 조회
     * @param bookmarks
     * @param member
     * @return 북마크 리스트
     */
    private fun convertToBookmarkDtoList(
        bookmarks: MutableList<Bookmark>,
        member: Member
    ): MutableList<BookmarkDto> {
        if (bookmarks.isEmpty()) {
            return mutableListOf()
        }
        //사용자가 쓴 리뷰 전체 조회
        val reviews: MutableList<Review> = reviewRepository.findAllByMember(member)
        //Key : bookId, Value: review
        val reviewMap = reviews.stream().collect(
            Collectors.toMap(
                Function { review: Review? -> review!!.book.id },
                Function { review: Review? -> review })
        )

        return bookmarks.stream().map { bookmark: Bookmark? ->
            val review = reviewMap.get(bookmark!!.book.id)
            BookmarkDto(bookmark, review)
        }.toList()
    }

    fun getLatestBookmark(member: Member): Bookmark {
        return bookmarkRepository.getBookmarkByMemberOrderByIdDesc(member)
            .orElseThrow<NoSuchElementException?>(Supplier { NoSuchElementException("조회가능한 북마크가 없습니다.") })
    }
}
