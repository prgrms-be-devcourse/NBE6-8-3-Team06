package com.back.global.initData

import com.back.domain.book.book.entity.Book
import com.back.domain.book.book.repository.BookRepository
import com.back.domain.book.book.service.BookService
import com.back.domain.book.category.entity.Category
import com.back.domain.book.category.repository.CategoryRepository
import com.back.domain.bookmarks.entity.Bookmark
import com.back.domain.bookmarks.repository.BookmarkRepository
import com.back.domain.bookmarks.service.BookmarkService
import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.repository.MemberRepository
import com.back.domain.member.member.service.MemberService
import com.back.domain.note.repository.NoteRepository
import com.back.domain.note.service.NoteService
import com.back.domain.review.review.dto.ReviewRequestDto
import com.back.domain.review.review.service.ReviewService
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import java.util.function.Supplier


@Configuration
@RequiredArgsConstructor
class BaseInitData(
    private val memberService: MemberService,
    private val categoryRepository: CategoryRepository,
    private val bookRepository: BookRepository,
    private val bookService: BookService,
    private val noteService: NoteService,
    private val noteRepository: NoteRepository,
    private val bookmarkService: BookmarkService,
    private val bookmarkRepository: BookmarkRepository,
    private val passwordEncoder: PasswordEncoder,
    private val memberRepository: MemberRepository,
    private val reviewService: ReviewService,
) {
    @Autowired
    @Lazy
    private lateinit var  self: BaseInitData



    @Bean
    fun baseInitDataApplicationRunner(): ApplicationRunner {
        return ApplicationRunner { args: ApplicationArguments ->
            self.initBookData() // 책 데이터 초기화
            self.initReviewData() // 리뷰 테스트 시 주석 해제
            self.initNoteData() // Note 관련 데이터
            self.initBookmarkData() // Bookmark 데이터 초기화
        }
    }

    @Transactional
    fun initReviewData() {
        val memberCount = 100
        if (memberRepository.count() > memberCount) {
            return  // 이미 데이터가 존재하면 초기화하지 않음
        }
        for (i in 1..memberCount) {
            memberService.join("testUser" + i, "email" + i + "@a.a", passwordEncoder.encode("password" + i))
        }
        val book = bookRepository.findAll().get(0) // 첫 번째 책을 가져옴
        for (i in 1..memberCount) {
            val member = memberRepository.findByEmail("email" + i + "@a.a")?:throw NoSuchElementException("멤버를 찾을 수 없습니다: ")
            reviewService.addReview(book.id, member, ReviewRequestDto("리뷰 ㅋㅋ " + i, 5, false))
        }

        //        Category category = categoryRepository.save(new Category("Test Category"));
//        bookRepository.save(new Book("Text Book", "Publisher", category));
    }

    @Transactional
    fun initBookData() {
        // 이미 데이터가 있으면 초기화하지 않음
        if (bookRepository.count() > 0) {
            println("책 데이터가 이미 존재합니다. 초기화하지 않습니다.")
            return
        }


        try {
            // 다양한 장르의 인기 도서들을 검색해서 DB에 저장
            val searchQueries = arrayOf(
                "자바의 정석",  // 프로그래밍
                "해리포터",  // 소설
                "미움받을 용기",  // 자기계발
                "사피엔스",  // 인문학
                "코스모스",  // 과학
                "1984",  // 소설
                "어린왕자" // 소설
            )

            val totalBooksAdded = 0

            for (query in searchQueries) {
                try {
                    // BookService의 searchBooks 메서드를 사용해서 데이터 수집
                    // 각 검색어당 최대 3권씩 가져오기

                    bookService.searchBooks(query, 3)


                    // API 호출 간격 조절 (너무 빠르게 호출하지 않도록)
                    Thread.sleep(500)
                } catch (e: Exception) {
                    // 하나의 검색어 실패가 전체를 중단시키지 않도록 continue
                    continue
                }
            }

            println("초기 데이터 로딩 완료. 총 " + bookRepository.count() + "권의 책이 저장되었습니다.")


            memberRepository.save(Member("리뷰쓰는놈", "asdf@asdf.com", "asdfasdfasdf"))
            reviewService.addReview(
                1,
                memberRepository.findByEmail("asdf@asdf.com")?:throw NoSuchElementException("멤버 못찾겠다요"),
                ReviewRequestDto(
                    "리뷰리뷰",
                    5,
                    false
                )
            )
        } catch (e: Exception) {
            println("초기 데이터 로딩 중 오류 발생: " + e.message)
        }
    }

    @Transactional
    fun initSpecificBooks() {
        val specificISBNs = arrayOf(
            "9788970503806",  // 자바의 정석 3판
            "9788966262281",  // 클린 코드
            "9788932473901",  // 해리포터와 마법사의 돌
            "9788996991304" // 미움받을 용기
        )

        var addedCount = 0
        for (isbn in specificISBNs) {
            try {
                bookService.getBookByIsbn(isbn)
                addedCount++

                // API 호출 간격 조절
                Thread.sleep(300)
            } catch (e: Exception) {
                println("ISBN: " + isbn + " 책 추가 실패: " + e.message)
            }
        }

        println("특정 ISBN 책 초기화 완료. 총 " + addedCount + "권의 책이 추가되었습니다.")
    }

    @Transactional
    fun initNoteData() {
        if (noteService.count() > 0) {
            return
        }

        val book = Book("Text Book", "Publisher", categoryRepository.save(Category("Test Category")))
        bookRepository.save(book)

        val member1 = memberService.join("유저1", "email1@naver.com", passwordEncoder.encode("12341234"))
        member1.updateRefreshToken("key1")
        val member2 = memberService.join("유저2", "email2@naver.com", passwordEncoder.encode("12341234"))
        member2.updateRefreshToken("key2")
        val member3 = memberService.join("유저3", "email3@naver.com", passwordEncoder.encode("12341234"))

        val bookmark1 = bookmarkRepository.save<Bookmark>(Bookmark(book, member1))
        val bookmark2 = bookmarkRepository.save<Bookmark>(Bookmark(book, member2))
        val id1 = bookmark1.id
        val id2 = bookmark2.id

        noteService.write(id1, "제목1", "내용1", null, member1)
        noteService.write(id1, "제목2", "내용2", "2", member1)
        noteService.write(id2, "제목3", "내용3", "3", member2)
        noteService.write(id2, "제목4", "내용4", "4", member2)
    }

    fun initBookmarkData() {
        if (bookmarkRepository.count() > 0) return

        var member: Member = memberRepository.findByEmail("email@test.com")
            ?:memberService.join("testUser", "email@test.com", passwordEncoder.encode("password"))
        val book1 = bookRepository.findById(1).get()
        val book2 = bookRepository.findById(2).get()
        val book3 = bookRepository.findById(3).get()
        val bookmark1 = bookmarkService.save(book1.id, member)
        val bookmark2 = bookmarkService.save(book2.id, member)
        bookmarkService.save(book3.id, member)
        //bookmarkService.modifyBookmark(member, bookmark1.getId(), "READ", LocalDateTime.of(2025,07,22,12,20), LocalDateTime.now(),book1.getTotalPage());
        //bookmarkService.modifyBookmark(member, bookmark2.getId(), "READING", LocalDateTime.now(), null, 101);
    }
}