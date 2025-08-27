package com.back.domain.review.review.controller;

import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.AuthTokenService;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.review.repository.ReviewRepository;
import com.back.domain.review.review.service.ReviewService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class ReviewControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private BookRepository bookRepository;

    ResultActions addReview(int bookId, String accessToken) throws Exception {
        return mvc.perform(
                post("/reviews/{book_id}", bookId)
                        .contentType("application/json")
                        .content("""
{
    "content": "이 책 정말 좋았어요!",
    "rate": 5
}
""").cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());
    }

    @Test
    @DisplayName("리뷰 작성")
    void t1() throws Exception {
        Member member = memberService.findByEmail("email1@a.a").get();
        String accessToken = authTokenService.genAccessToken(member);
        Book book = bookRepository.findAll().get(0);
        ResultActions resultActions = addReview(book.getId(),accessToken);
        Review review = reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));
        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("create"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("Reviews fetched successfully"))
                ;

        assertThat(review.getId()).isGreaterThan(0);
        assertThat(review.getContent()).isEqualTo("이 책 정말 좋았어요!");
        assertThat(review.getRate()).isEqualTo(5);
    }

    @Test
    @DisplayName("리뷰 작성 여러번 - 실패")
    void t1_2() throws Exception {
        Member member = memberService.findByEmail("email1@a.a").get();
        String accessToken = authTokenService.genAccessToken(member);
        Book book = bookRepository.findAll().get(0);
        addReview(book.getId(),accessToken);
        ResultActions resultActions = addReview(book.getId(),accessToken);
        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("create"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("Review already exists"));
        assertThat(reviewService.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("리뷰 삭제")
    void t2() throws Exception {
        Member member = memberService.findByEmail("email1@a.a").get();
        String accessToken = authTokenService.genAccessToken(member);
        Book book = bookRepository.findAll().get(0);
        addReview(book.getId(),accessToken);
        Review review = reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));
        assertThat(review.getId()).isGreaterThan(0);

        ResultActions resultActions = mvc.perform(
                delete("/reviews/{book_id}", 1)
                        .contentType("application/json")
                        .cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("Review deleted successfully"))
        ;
        assertThat(reviewService.findLatest()).isEmpty();
    }

    @Test
    @DisplayName("리뷰 수정")
    void t3() throws Exception {
        Member member = memberService.findByEmail("email1@a.a").get();
        String accessToken = authTokenService.genAccessToken(member);
        Book book = bookRepository.findAll().get(0);
        addReview(book.getId(),accessToken);
        Review review = reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));
        assertThat(review.getId()).isGreaterThan(0);
        ResultActions resultActions = mvc.perform(
                put("/reviews/{book_id}", 1)
                        .contentType("application/json")
                        .content("""
{
    "content": "다시 읽다보니 그렇게 좋지는 않네요.",
    "rate": 4
}
""").cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());
        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("Review modified successfully"))
        ;
        review = reviewService.findLatest().orElseThrow(() -> new RuntimeException("리뷰가 없습니다."));
        assertThat(review.getId()).isGreaterThan(0);
        assertThat(review.getContent()).isEqualTo("다시 읽다보니 그렇게 좋지는 않네요.");
        assertThat(review.getRate()).isEqualTo(4);
    }

    @Test
    @DisplayName("리뷰 조회")
    void t4() throws Exception{
        Member member = memberService.findByEmail("email1@a.a").get();
        String accessToken = authTokenService.genAccessToken(member);
        Book book = bookRepository.findAll().get(0);
        addReview(book.getId(),accessToken);
        Review review = reviewService.findLatest().orElseThrow(()-> new RuntimeException("리뷰가 없습니다."));
        assertThat(review.getId()).isGreaterThan(0);

        ResultActions resultActions = mvc.perform(
                get("/reviews/{book_id}", 1)
                        .cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("getReview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("Review read successfully"))
                .andExpect(jsonPath("$.data.content").value(review.getContent()))
                .andExpect(jsonPath("$.data.rate").value(review.getRate()))
        ;
    }

    @Test
    @DisplayName("리뷰 조회 - 실패 (리뷰 없음)")
    void t4_2() throws Exception{
        Member member = memberService.findByEmail("email1@a.a").get();
        String accessToken = authTokenService.genAccessToken(member);
        Book book = bookRepository.findAll().get(0);

        ResultActions resultActions = mvc.perform(
                get("/reviews/{book_id}", book.getId())
                        .cookie(new Cookie("accessToken", accessToken))
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(ReviewController.class))
                .andExpect(handler().methodName("getReview"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("Review not found"))
        ;
    }
}
