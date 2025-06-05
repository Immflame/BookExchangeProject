package com.example.book_exchange.service;

import com.example.book_exchange.exception.ResourceNotFoundException;
import com.example.book_exchange.model.Review;
import com.example.book_exchange.model.User;
import com.example.book_exchange.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Review review;
    private User reviewer;
    private User reviewee;

    @BeforeEach
    void setUp() {
        reviewer = User.builder()
                .id(1L)
                .username("reviewer")
                .build();

        reviewee = User.builder()
                .id(2L)
                .username("reviewee")
                .build();

        review = Review.builder()
                .id(1L)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .rating(5)
                .comment("Great service!")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllReviews_ShouldReturnAllReviews() {
        when(reviewRepository.findAll()).thenReturn(Arrays.asList(review));
        List<Review> result = reviewService.getAllReviews();
        assertEquals(1, result.size());
        assertEquals("Great service!", result.get(0).getComment());
    }

    @Test
    void getReviewById_ShouldReturnReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        Optional<Review> result = reviewService.getReviewById(1L);
        assertTrue(result.isPresent());
        assertEquals(5, result.get().getRating());
    }

    @Test
    void getReviewById_NotFound_ShouldReturnEmpty() {
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.empty());
        Optional<Review> result = reviewService.getReviewById(999L);
        assertFalse(result.isPresent());
    }

    @Test
    void createReview_ShouldReturnSavedReview() {
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        Review result = reviewService.createReview(review);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void updateReview_ShouldUpdateRatingAndComment() {
        Review updatedReview = Review.builder()
                .rating(4)
                .comment("Good, but could be better")
                .build();

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Review result = reviewService.updateReview(1L, updatedReview);

        assertEquals(4, result.getRating());
        assertEquals("Good, but could be better", result.getComment());
        assertSame(reviewer, result.getReviewer());
        assertSame(reviewee, result.getReviewee());
    }

    @Test
    void updateReview_NotFound_ShouldThrowException() {
        Review updatedReview = new Review();
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> reviewService.updateReview(1L, updatedReview));
    }

    @Test
    void deleteReview_ShouldDeleteExistingReview() {
        when(reviewRepository.existsById(1L)).thenReturn(true);

        reviewService.deleteReview(1L);

        verify(reviewRepository).existsById(1L);
        verify(reviewRepository).deleteById(1L);
    }

    @Test
    void deleteReview_NotFound_ShouldThrowException() {
        when(reviewRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> reviewService.deleteReview(1L));

        verify(reviewRepository).existsById(1L);
        verify(reviewRepository, never()).deleteById(anyLong());
    }

    @Test
    void getReviewsByRevieweeId_ShouldReturnReviews() {
        when(reviewRepository.findByRevieweeId(2L)).thenReturn(Arrays.asList(review));
        List<Review> result = reviewService.getReviewsByRevieweeId(2L);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getReviewee().getId());
    }

    @Test
    void getReviewsByRevieweeId_NoReviews_ShouldReturnEmptyList() {
        when(reviewRepository.findByRevieweeId(anyLong())).thenReturn(Collections.emptyList());
        List<Review> result = reviewService.getReviewsByRevieweeId(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void getReviewsByReviewerId_ShouldReturnReviews() {
        when(reviewRepository.findByReviewerId(1L)).thenReturn(Arrays.asList(review));
        List<Review> result = reviewService.getReviewsByReviewerId(1L);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getReviewer().getId());
    }

    @Test
    void getReviewsByReviewerId_NoReviews_ShouldReturnEmptyList() {
        when(reviewRepository.findByReviewerId(anyLong())).thenReturn(Collections.emptyList());
        List<Review> result = reviewService.getReviewsByReviewerId(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateReview_ShouldNotChangeReviewerAndReviewee() {
        User newReviewer = User.builder().id(3L).build();
        User newReviewee = User.builder().id(4L).build();

        Review updatedReview = Review.builder()
                .reviewer(newReviewer)
                .reviewee(newReviewee)
                .rating(3)
                .comment("Updated comment")
                .build();

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Review result = reviewService.updateReview(1L, updatedReview);

        assertEquals(3, result.getRating());
        assertEquals("Updated comment", result.getComment());
        assertEquals(1L, result.getReviewer().getId());
        assertEquals(2L, result.getReviewee().getId());
    }

    @Test
    void updateReview_ShouldNotChangeCreatedAt() {
        LocalDateTime originalCreatedAt = review.getCreatedAt();
        Review updatedReview = Review.builder()
                .rating(2)
                .comment("Bad experience")
                .createdAt(LocalDateTime.now().plusDays(1))
                .build();

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Review result = reviewService.updateReview(1L, updatedReview);

        assertEquals(originalCreatedAt, result.getCreatedAt());
    }
}