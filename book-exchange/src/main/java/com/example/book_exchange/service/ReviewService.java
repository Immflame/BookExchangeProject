package com.example.book_exchange.service;

import com.example.book_exchange.exception.ResourceNotFoundException;
import com.example.book_exchange.model.Review;
import com.example.book_exchange.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    public Review createReview(Review review) {
        return reviewRepository.save(review);
    }

    public Review updateReview(Long id, Review updatedReview) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        review.setRating(updatedReview.getRating());
        review.setComment(updatedReview.getComment());

        return reviewRepository.save(review);
    }

    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }

    public List<Review> getReviewsByRevieweeId(Long revieweeId) {
        return reviewRepository.findByRevieweeId(revieweeId);
    }

    public List<Review> getReviewsByReviewerId(Long reviewerId) {
        return reviewRepository.findByReviewerId(reviewerId);
    }

    public void deleteReviewsByUserId(Long userId) {
        List<Review> reviewsAsReviewer = reviewRepository.findByReviewerId(userId);
        reviewRepository.deleteAll(reviewsAsReviewer);

        List<Review> reviewsAsReviewee = reviewRepository.findByRevieweeId(userId);
        reviewRepository.deleteAll(reviewsAsReviewee);
    }
}