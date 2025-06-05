package com.example.book_exchange.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import com.example.book_exchange.dto.*;
import com.example.book_exchange.exception.*;
import com.example.book_exchange.model.Review;
import com.example.book_exchange.model.User;
import com.example.book_exchange.service.AuthService;
import com.example.book_exchange.service.ReviewService;
import com.example.book_exchange.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private ReviewService reviewService;
    @Mock private UserService userService;
    @Mock private AuthService authService;

    @InjectMocks private ReviewController reviewController;

    private User reviewer;
    private User reviewee;
    private Review testReview;
    private ReviewCreateDto testCreateDto;
    private ReviewUpdateDto testUpdateDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        reviewer = new User(1L, "reviewer", "pass", "USER");
        reviewee = new User(2L, "reviewee", "pass", "USER");

        testReview = Review.builder()
                .id(1L)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .rating(5)
                .comment("Great service")
                .createdAt(LocalDateTime.now())
                .build();

        testCreateDto = new ReviewCreateDto();
        testCreateDto.setToken("valid-token");
        testCreateDto.setRating(5);
        testCreateDto.setComment("Great service");
        testCreateDto.setRevieweeId(2L);

        testUpdateDto = new ReviewUpdateDto();
        testUpdateDto.setToken("valid-token");
        testUpdateDto.setRating(4);
        testUpdateDto.setComment("Good service");
    }

    @Test
    void getAllReviews_Success() throws Exception {
        when(reviewService.getAllReviews()).thenReturn(Collections.singletonList(testReview));
        doNothing().when(authService).validateAdminToken("admin-token");

        mockMvc.perform(get("/reviews/getAllReviews/admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rating").value(5));
    }

    @Test
    void getAllReviews_InvalidToken() throws Exception {
        doThrow(new InvalidTokenException("Invalid token"))
                .when(authService).validateAdminToken("invalid-token");

        mockMvc.perform(get("/reviews/getAllReviews/invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    void getAllReviews_NotAdmin() throws Exception {
        doThrow(new PermissionDeniedException("Admin role required"))
                .when(authService).validateAdminToken("user-token");

        mockMvc.perform(get("/reviews/getAllReviews/user-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Admin role required"));
    }

    @Test
    void getReviewById_Success() throws Exception {
        when(reviewService.getReviewById(1L)).thenReturn(Optional.of(testReview));

        mockMvc.perform(get("/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void getReviewById_NotFound() throws Exception {
        when(reviewService.getReviewById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/reviews/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Review not found"));
    }

    @Test
    void createReview_Success() throws Exception {
        when(authService.validateToken("valid-token")).thenReturn(new UserInfo(1L, "reviewer", "USER", true));
        when(userService.getUserById(1L)).thenReturn(Optional.of(reviewer));
        when(userService.getUserById(2L)).thenReturn(Optional.of(reviewee));
        when(reviewService.createReview(any(Review.class))).thenReturn(testReview);

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void createReview_InvalidData() throws Exception {
        ReviewCreateDto invalidDto = new ReviewCreateDto();
        invalidDto.setToken("valid-token");
        invalidDto.setRating(6);
        invalidDto.setRevieweeId(2L);

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("must be less than or equal to 5")));
    }


    @Test
    void createReview_InvalidToken() throws Exception {
        ReviewCreateDto invalidTokenDto = new ReviewCreateDto();
        invalidTokenDto.setToken("invalid-token");
        invalidTokenDto.setRating(5);
        invalidTokenDto.setRevieweeId(2L);

        when(authService.validateToken("invalid-token"))
                .thenThrow(new InvalidTokenException("Invalid token"));

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTokenDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    void createReview_ReviewerNotFound() throws Exception {
        when(authService.validateToken("valid-token")).thenReturn(new UserInfo(1L, "reviewer", "USER", true));
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCreateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Reviewer not found"));
    }

    @Test
    void createReview_RevieweeNotFound() throws Exception {
        when(authService.validateToken("valid-token")).thenReturn(new UserInfo(1L, "reviewer", "USER", true));
        when(userService.getUserById(1L)).thenReturn(Optional.of(reviewer));
        when(userService.getUserById(2L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCreateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Reviewee not found"));
    }

    @Test
    void createReview_SelfReview() throws Exception {
        // Попытка оставить отзыв самому себе
        ReviewCreateDto selfReviewDto = new ReviewCreateDto();
        selfReviewDto.setToken("valid-token");
        selfReviewDto.setRating(5);
        selfReviewDto.setRevieweeId(1L);

        when(authService.validateToken("valid-token")).thenReturn(new UserInfo(1L, "reviewer", "USER", true));
        when(userService.getUserById(1L)).thenReturn(Optional.of(reviewer));

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(selfReviewDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot review yourself"));
    }

    @Test
    void updateReview_Success() throws Exception {
        when(authService.validateToken("valid-token")).thenReturn(new UserInfo(1L, "reviewer", "USER", true));
        when(userService.getUserById(1L)).thenReturn(Optional.of(reviewer));
        when(reviewService.getReviewById(1L)).thenReturn(Optional.of(testReview));
        when(reviewService.updateReview(anyLong(), any(Review.class))).thenReturn(testReview);

        mockMvc.perform(patch("/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void updateReview_NotFound() throws Exception {

        when(reviewService.getReviewById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUpdateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Review not found"));
    }

    @Test
    void updateReview_PermissionDenied() throws Exception {
        User otherUser = new User(3L, "other", "pass", "USER");
        Review otherReview = Review.builder().id(1L).reviewer(otherUser).build();

        when(authService.validateToken("valid-token")).thenReturn(new UserInfo(1L, "reviewer", "USER", true));
        when(userService.getUserById(1L)).thenReturn(Optional.of(reviewer));
        when(reviewService.getReviewById(1L)).thenReturn(Optional.of(otherReview));

        mockMvc.perform(patch("/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUpdateDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Permission denied"));
    }

    @Test
    void updateReview_InvalidData() throws Exception {
        ReviewUpdateDto invalidDto = new ReviewUpdateDto();
        invalidDto.setToken("valid-token");
        invalidDto.setRating(6);

        mockMvc.perform(patch("/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("rating: Rating must be at most 5")));
    }

    @Test
    void deleteReview_Success() throws Exception {
        TokenDto tokenDto = new TokenDto();
        tokenDto.setToken("valid-token");

        when(authService.validateToken("valid-token")).thenReturn(new UserInfo(1L, "reviewer", "USER", true));
        when(userService.getUserById(1L)).thenReturn(Optional.of(reviewer));
        when(reviewService.getReviewById(1L)).thenReturn(Optional.of(testReview));

        mockMvc.perform(delete("/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenDto)))
                .andExpect(status().isNoContent());

        verify(reviewService).deleteReview(1L);
    }

    @Test
    void deleteReview_NotFound() throws Exception {
        TokenDto tokenDto = new TokenDto();
        tokenDto.setToken("valid-token");

        when(authService.validateToken("valid-token")).thenReturn(new UserInfo(1L, "reviewer", "USER", true));
        when(userService.getUserById(1L)).thenReturn(Optional.of(reviewer));
        when(reviewService.getReviewById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Review not found"));
    }

    @Test
    void deleteReview_PermissionDenied() throws Exception {
        TokenDto tokenDto = new TokenDto();
        tokenDto.setToken("valid-token");

        User otherUser = new User(3L, "other", "pass", "USER");
        Review otherReview = Review.builder().id(1L).reviewer(otherUser).build();

        when(authService.validateToken("valid-token")).thenReturn(new UserInfo(1L, "reviewer", "USER", true));
        when(userService.getUserById(1L)).thenReturn(Optional.of(reviewer));
        when(reviewService.getReviewById(1L)).thenReturn(Optional.of(otherReview));

        mockMvc.perform(delete("/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Permission denied"));
    }

    @Test
    void getByRevieweeId_Success() throws Exception {
        when(reviewService.getReviewsByRevieweeId(2L)).thenReturn(Collections.singletonList(testReview));

        mockMvc.perform(get("/reviews/reviewee/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rating").value(5));
    }

    @Test
    void getByReviewerId_Success() throws Exception {
        when(reviewService.getReviewsByReviewerId(1L)).thenReturn(Collections.singletonList(testReview));

        mockMvc.perform(get("/reviews/reviewer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rating").value(5));
    }

    @Test
    void getByRevieweeId_InvalidId() throws Exception {
        mockMvc.perform(get("/reviews/reviewee/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid reviewee ID"));
    }

    @Test
    void getByReviewerId_InvalidId() throws Exception {
        mockMvc.perform(get("/reviews/reviewer/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid reviewer ID"));
    }
}
