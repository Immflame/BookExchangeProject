package com.example.book_exchange.controller;

import com.example.book_exchange.dto.*;
import com.example.book_exchange.exception.*;
import com.example.book_exchange.model.Review;
import com.example.book_exchange.model.User;
import com.example.book_exchange.service.ReviewService;
import com.example.book_exchange.service.AuthService;
import com.example.book_exchange.service.UserService;
import com.example.book_exchange.util.ValidationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Review Controller", description = "Operations with review")
@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final AuthService authService;

    private String extractToken(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        throw new InvalidTokenException("Invalid authorization header");
    }

    @Operation(summary = "Get all reviews (only for admin)")
    @GetMapping("/getAllReviews")
    public ResponseEntity<List<ReviewResponseDto>> getAllReviews(
            @RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        authService.validateAdminToken(token);
        return ResponseEntity.ok(reviewService.getAllReviews().stream()
                .map(this::convertToDto)
                .toList());
    }

    @Operation(summary = "Get review by review_id")
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDto> getReviewById(@PathVariable Long id) {
        ValidationUtils.validateId(id, "review");
        return ResponseEntity.ok(convertToDto(
                reviewService.getReviewById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Review not found"))
        ));
    }

    @Operation(summary = "Create new review")
    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ReviewCreateDto dto) {
        String token = extractToken(authHeader);
        User reviewer = validateReviewer(token);
        User reviewee = userService.getUserById(dto.getRevieweeId())
                .orElseThrow(() -> new ResourceNotFoundException("Reviewee not found"));

        if (reviewer.getId().equals(reviewee.getId())) {
            throw new InvalidRequestException("Cannot review yourself");
        }

        Review newReview = buildReview(dto, reviewer, reviewee);
        return ResponseEntity.status(201)
                .body(convertToDto(reviewService.createReview(newReview)));
    }

    @Operation(summary = "Update review")
    @PatchMapping("/{id}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateDto dto) {
        String token = extractToken(authHeader);
        Review existingReview = reviewService.getReviewById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        User reviewer = validateReviewPermissions(id, token);

        Review updatedReview = Review.builder()
                .id(id)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .reviewer(reviewer)
                .reviewee(existingReview.getReviewee())
                .createdAt(existingReview.getCreatedAt())
                .build();

        return ResponseEntity.ok(convertToDto(
                reviewService.updateReview(id, updatedReview)
        ));
    }

    @Operation(summary = "Delete review by review_id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        String token = extractToken(authHeader);
        validateReviewPermissions(id, token);
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get review by revieweeId")
    @GetMapping("/reviewee/{revieweeId}")
    public ResponseEntity<List<ReviewResponseDto>> getByRevieweeId(@PathVariable Long revieweeId) {
        ValidationUtils.validateId(revieweeId, "reviewee");
        return ResponseEntity.ok(
                reviewService.getReviewsByRevieweeId(revieweeId).stream()
                        .map(this::convertToDto)
                        .toList()
        );
    }

    @Operation(summary = "Get review by reviewedId")
    @GetMapping("/reviewer/{reviewerId}")
    public ResponseEntity<List<ReviewResponseDto>> getByReviewerId(@PathVariable Long reviewerId) {
        ValidationUtils.validateId(reviewerId, "reviewer");
        return ResponseEntity.ok(
                reviewService.getReviewsByReviewerId(reviewerId).stream()
                        .map(this::convertToDto)
                        .toList()
        );
    }

    private User validateReviewer(String token) {
        UserInfo userInfo = authService.validateToken(token);
        return userService.getUserById(userInfo.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));
    }

    private User validateReviewPermissions(Long reviewId, String token) {
        User reviewer = validateReviewer(token);
        Review review = reviewService.getReviewById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getReviewer().getId().equals(reviewer.getId())) {
            throw new PermissionDeniedException("Permission denied");
        }
        return reviewer;
    }

    private ReviewResponseDto convertToDto(Review review) {
        return ReviewResponseDto.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .reviewerId(review.getReviewer().getId())
                .reviewerUsername(review.getReviewer().getUsername())
                .revieweeId(review.getReviewee().getId())
                .revieweeUsername(review.getReviewee().getUsername())
                .build();
    }

    private Review buildReview(ReviewCreateDto dto, User reviewer, User reviewee) {
        return Review.builder()
                .rating(dto.getRating())
                .comment(dto.getComment())
                .reviewer(reviewer)
                .reviewee(reviewee)
                .build();
    }
}