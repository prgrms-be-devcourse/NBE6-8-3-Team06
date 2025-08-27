package com.back.domain.bookmark.controller;

import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.bookmarks.controller.BookmarkController;
import com.back.domain.bookmarks.dto.BookmarkDto;
import com.back.domain.bookmarks.dto.BookmarkDetailDto;
import com.back.domain.bookmarks.dto.BookmarkReadStatesDto;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.bookmarks.service.BookmarkService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import jakarta.servlet.http.Cookie;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class BookmarkControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BookmarkService bookmarkService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    int id =0;
    @BeforeEach
    void setup() {

        Member member =memberService.findByEmail("email@test.com").orElse(null);
        if(member==null) {
            member = memberService.join("testUser", "email@test.com", passwordEncoder.encode("password"));
        }

        bookmarkService.save(1, member);
        id = bookmarkService.getLatestBookmark(member).getId();
        System.out.println("id : "+id);
    }

    @Test
    @DisplayName("북마크 추가")
    void t1() throws Exception {
        Member member = memberService.findByEmail("email@test.com").get();
        String accessToken = memberService.geneAccessToken(member);
        Book book = bookRepository.findById(3).get();
        ResultActions resultActions = mvc.perform(
                post("/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "bookId" : %d
                        }
                        """.formatted(book.getId()))
                        .cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(BookmarkController.class))
                .andExpect(handler().methodName("addBookmark"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d 번 책이 내 책 목록에 추가되었습니다.".formatted(book.getId())));
    }

    @Test
    @DisplayName("북마크 단건 조회")
    void t2() throws Exception {
        Member member = memberService.findByEmail("email@test.com").get();
        String accessToken = memberService.geneAccessToken(member);
        ResultActions resultActions = mvc
                .perform(
                        get("/bookmarks/"+id)
                                .cookie(new Cookie("accessToken", accessToken))
                )
                .andDo(print());
        BookmarkDetailDto bookmarkDto = bookmarkService.getBookmarkById(member, id);

        resultActions
                .andExpect(handler().handlerType(BookmarkController.class))
                .andExpect(handler().methodName("getBookmark"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 조회 성공".formatted(bookmarkDto.bookmarkDto().id())))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.bookId").value(bookmarkDto.bookmarkDto().bookId()))
                .andExpect(jsonPath("$.data.readState").value(bookmarkDto.bookmarkDto().readState()))
                .andExpect(jsonPath("$.data.readPage").value(bookmarkDto.bookmarkDto().readPage()))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(bookmarkDto.bookmarkDto().createDate().toString().substring(0,18))))
                .andExpect(jsonPath("$.data.startReadDate").isEmpty())
                .andExpect(jsonPath("$.data.endReadDate").isEmpty())
                .andExpect(jsonPath("$.data.readingDuration").value(bookmarkDto.readingDuration()))
                .andExpect(jsonPath("$.data.book.id").value(bookmarkDto.bookmarkDto().bookId()))
                .andExpect(jsonPath("$.data.book.isbn13").value(bookmarkDto.bookmarkDto().book().isbn13()))
                .andExpect(jsonPath("$.data.book.title").value(bookmarkDto.bookmarkDto().book().title()))
                .andExpect(jsonPath("$.data.book.imageUrl").value(bookmarkDto.bookmarkDto().book().imageUrl()))
                .andExpect(jsonPath("$.data.book.publisher").value(bookmarkDto.bookmarkDto().book().publisher()))
                .andExpect(jsonPath("$.data.book.totalPage").value(bookmarkDto.bookmarkDto().book().totalPage()))
                .andExpect(jsonPath("$.data.book.avgRate").value(bookmarkDto.bookmarkDto().book().avgRate()))
                .andExpect(jsonPath("$.data.book.category").value(bookmarkDto.bookmarkDto().book().category()))
//                .andExpect(jsonPath("$.data.book.authors").value(bookmarkDto.bookmarkDto().book().authors()))
                .andExpect(jsonPath("$.data.book.publishDate").value(Matchers.startsWith(bookmarkDto.bookmarkDto().book().publishDate().toString().substring(0,16))));
    }

    @Test
    @DisplayName("결제 단건 실패")
    void t3() throws Exception {
        int id = Integer.MAX_VALUE;
        Member member = memberService.findByEmail("email@test.com").get();
        String accessToken = memberService.geneAccessToken(member);
        ResultActions resultActions = mvc
                .perform(
                        get("/bookmarks/"+id)
                                .cookie(new Cookie("accessToken", accessToken))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(BookmarkController.class))
                .andExpect(handler().methodName("getBookmark"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("%d번 데이터가 없습니다.".formatted(id)));
    }

    @Test
    @DisplayName("북마크 다건 조회 - 목록")
    void t4() throws Exception {
        Member member = memberService.findByEmail("email@test.com").get();
        String accessToken = memberService.geneAccessToken(member);
        ResultActions resultActions = mvc
                .perform(
                        get("/bookmarks/list")
                                .cookie(new Cookie("accessToken", accessToken))
                )
                .andDo(print());

        List<BookmarkDto> bookmarksDtoList = bookmarkService.toList(member);

        resultActions
                .andExpect(handler().handlerType(BookmarkController.class))
                .andExpect(handler().methodName("getBookmarksToList"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(bookmarksDtoList.size()));

        for(int i=0;i<bookmarksDtoList.size();i++) {
            BookmarkDto bookmarksDto = bookmarksDtoList.get(i);
            resultActions
                    .andExpect(jsonPath("$.data[%d].id".formatted(i)).value(bookmarksDto.id()))
                    .andExpect(jsonPath("$.data[%d].bookId".formatted(i)).value(bookmarksDto.bookId()))
                    .andExpect(jsonPath("$.data[%d].readState".formatted(i)).value(bookmarksDto.readState()))
                    .andExpect(jsonPath("$.data[%d].readPage".formatted(i)).value(bookmarksDto.readPage()))
                    .andExpect(jsonPath("$.data[%d].readingRate".formatted(i)).value(bookmarksDto.readingRate()))
                    .andExpect(jsonPath("$.data[%d].createDate".formatted(i)).value(Matchers.startsWith(bookmarksDto.createDate().toString().substring(0,18))))
                    .andExpect(jsonPath("$.data[%d].book.id".formatted(i)).value(bookmarksDto.book().id()))
                    .andExpect(jsonPath("$.data[%d].book.isbn13".formatted(i)).value(bookmarksDto.book().isbn13()))
                    .andExpect(jsonPath("$.data[%d].book.title".formatted(i)).value(bookmarksDto.book().title()))
                    .andExpect(jsonPath("$.data[%d].book.imageUrl".formatted(i)).value(bookmarksDto.book().imageUrl()))
                    .andExpect(jsonPath("$.data[%d].book.publisher".formatted(i)).value(bookmarksDto.book().publisher()))
                    .andExpect(jsonPath("$.data[%d].book.totalPage".formatted(i)).value(bookmarksDto.book().totalPage()))
                    .andExpect(jsonPath("$.data[%d].book.avgRate".formatted(i)).value(bookmarksDto.book().avgRate()))
                    .andExpect(jsonPath("$.data[%d].book.category".formatted(i)).value(bookmarksDto.book().category()))
//                .andExpect(jsonPath("$.data[%d].book.authors".formatted(i)).value(bookmarksDto.book().authors()))
                    .andExpect(jsonPath("$.data[%d].book.publishDate".formatted(i)).value(Matchers.startsWith(bookmarksDto.book().publishDate().toString().substring(0,16))));
        }
    }

    @Test
    @DisplayName("북마크 다건 조회 - 페이지")
    void t5() throws Exception {
        Member member = memberService.findByEmail("email@test.com").get();
        String accessToken = memberService.geneAccessToken(member);
        ResultActions resultActions = mvc
                .perform(
                        get("/bookmarks")
                                .cookie(new Cookie("accessToken", accessToken))
                )
                .andDo(print());

        Page<BookmarkDto> bookmarksDtoPage = bookmarkService.toPage(member,0,10, "createDate,desc",null, null, null);

        resultActions
                .andExpect(handler().handlerType(BookmarkController.class))
                .andExpect(handler().methodName("getBookmarksToPage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data.length()").value(bookmarksDtoPage.getContent().size()))
                .andExpect(jsonPath("$.data.pageNumber").value(bookmarksDtoPage.getNumber()))
                .andExpect(jsonPath("$.data.pageSize").value(bookmarksDtoPage.getSize()))
                .andExpect(jsonPath("$.data.totalPages").value(bookmarksDtoPage.getTotalPages()))
                .andExpect(jsonPath("$.data.totalElements").value(bookmarksDtoPage.getTotalElements()))
                .andExpect(jsonPath("$.data.isLast").value(bookmarksDtoPage.isLast()));

        for(int i=0;i<bookmarksDtoPage.getContent().size(); i++) {
            BookmarkDto bookmarksDto = bookmarksDtoPage.getContent().get(i);
            resultActions
                    .andExpect(jsonPath("$.data.data[%d].id".formatted(i)).value(bookmarksDto.id()))
                    .andExpect(jsonPath("$.data.data[%d].bookId".formatted(i)).value(bookmarksDto.bookId()))
                    .andExpect(jsonPath("$.data.data[%d].readState".formatted(i)).value(bookmarksDto.readState()))
                    .andExpect(jsonPath("$.data.data[%d].readPage".formatted(i)).value(bookmarksDto.readPage()))
                    .andExpect(jsonPath("$.data.data[%d].readingRate".formatted(i)).value(bookmarksDto.readingRate()))
                    .andExpect(jsonPath("$.data.data[%d].createDate".formatted(i)).value(Matchers.startsWith(bookmarksDto.createDate().toString().substring(0,18))))
                    .andExpect(jsonPath("$.data.data[%d].book.id".formatted(i)).value(bookmarksDto.book().id()))
                    .andExpect(jsonPath("$.data.data[%d].book.isbn13".formatted(i)).value(bookmarksDto.book().isbn13()))
                    .andExpect(jsonPath("$.data.data[%d].book.title".formatted(i)).value(bookmarksDto.book().title()))
                    .andExpect(jsonPath("$.data.data[%d].book.imageUrl".formatted(i)).value(bookmarksDto.book().imageUrl()))
                    .andExpect(jsonPath("$.data.data[%d].book.publisher".formatted(i)).value(bookmarksDto.book().publisher()))
                    .andExpect(jsonPath("$.data.data[%d].book.totalPage".formatted(i)).value(bookmarksDto.book().totalPage()))
                    .andExpect(jsonPath("$.data.data[%d].book.avgRate".formatted(i)).value(bookmarksDto.book().avgRate()))
                    .andExpect(jsonPath("$.data.data[%d].book.category".formatted(i)).value(bookmarksDto.book().category()));
//                .andExpect(jsonPath("$.data.data[%d].book.authors".formatted(i)).value(bookmarksDto.book().authors()))
//                    .andExpect(jsonPath("$.data.data[%d].book.publishDate".formatted(i)).value(Matchers.startsWith(bookmarksDto.book().publishDate().toString().substring(0,16))));
        }
    }

    @Test
    @DisplayName("북마크 수정")
    void t6() throws Exception {
        Member member = memberService.findByEmail("email@test.com").get();
        String accessToken = memberService.geneAccessToken(member);
        ResultActions resultActions = mvc.perform(
                put("/bookmarks/"+id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "readState" : "READING",
                          "startReadDate" : "2025-07-22T10:00:00",
                          "readPage" : 100
                        }
                        """)
                        .cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());

        Bookmark bookmark = bookmarkService.findById(id);
        resultActions
                .andExpect(handler().handlerType(BookmarkController.class))
                .andExpect(handler().methodName("modifyBookmark"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 북마크가 수정되었습니다.".formatted(id)))
                .andExpect(jsonPath("$.data.bookId").value(bookmark.getBook().getId()))
                .andExpect(jsonPath("$.data.readState").value(bookmark.getReadState().toString()))
                .andExpect(jsonPath("$.data.readPage").value(bookmark.getReadPage()))
                .andExpect(jsonPath("$.data.readingRate").value(bookmark.calculateReadingRate()))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(bookmark.getCreateDate().toString().substring(0,16))))
                .andExpect(jsonPath("$.data.startReadDate").value(Matchers.startsWith(bookmark.getStartReadDate().toString().substring(0,16))))
                .andExpect(jsonPath("$.data.endReadDate").isEmpty())
                .andExpect(jsonPath("$.data.book.id").value(bookmark.getBook().getId()))
                .andExpect(jsonPath("$.data.book.isbn13").value(bookmark.getBook().getIsbn13()))
                .andExpect(jsonPath("$.data.book.title").value(bookmark.getBook().getTitle()))
                .andExpect(jsonPath("$.data.book.imageUrl").value(bookmark.getBook().getImageUrl()))
                .andExpect(jsonPath("$.data.book.publisher").value(bookmark.getBook().getPublisher()))
                .andExpect(jsonPath("$.data.book.totalPage").value(bookmark.getBook().getTotalPage()))
                .andExpect(jsonPath("$.data.book.avgRate").value(bookmark.getBook().getAvgRate()))
                .andExpect(jsonPath("$.data.book.category").value(bookmark.getBook().getCategory().getName()))
//                .andExpect(jsonPath("$.data.book.authors").value(bookmark.book().authors()))
                .andExpect(jsonPath("$.data.book.publishDate").value(Matchers.startsWith(bookmark.getBook().getPublishedDate().toString().substring(0,16))));
    }

    @Test
    @DisplayName("북마크 삭제")
    void t7() throws Exception {
        Member member = memberService.findByEmail("email@test.com").get();
        String accessToken = memberService.geneAccessToken(member);
        ResultActions resultActions = mvc
                .perform(
                        delete("/bookmarks/"+id)
                                .cookie(new Cookie("accessToken", accessToken))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(BookmarkController.class))
                .andExpect(handler().methodName("deleteBookmark"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d 북마크가 삭제되었습니다.".formatted(id)));
    }

    @Test
    @DisplayName("북마크 내책 목록 상태 조회")
    void t8() throws Exception {
        Member member = memberService.findByEmail("email@test.com").get();
        String accessToken = memberService.geneAccessToken(member);
        ResultActions resultActions = mvc
                .perform(
                        get("/bookmarks/read-states")
                                .cookie(new Cookie("accessToken", accessToken))
                )
                .andDo(print());

        BookmarkReadStatesDto bookmarkReadStatesDto = bookmarkService.getReadStatesCount(member, null, null, null);

        resultActions
                .andExpect(handler().handlerType(BookmarkController.class))
                .andExpect(handler().methodName("getBookmarkReadStates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("조회 성공"))
                .andExpect(jsonPath("$.data.totalCount").value(bookmarkReadStatesDto.totalCount()))
                .andExpect(jsonPath("$.data.avgRate").value(bookmarkReadStatesDto.avgRate()))
                .andExpect(jsonPath("$.data.readState.READ").value(bookmarkReadStatesDto.readState().READ()))
                .andExpect(jsonPath("$.data.readState.READING").value(bookmarkReadStatesDto.readState().READING()))
                .andExpect(jsonPath("$.data.readState.WISH").value(bookmarkReadStatesDto.readState().WISH()));
    }
}
