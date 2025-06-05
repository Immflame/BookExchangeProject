import api from './apiService';

export const getReviewsByRevieweeId = (revieweeId) => {
  return api.get(`/reviews/reviewee/${revieweeId}`);
};

export const createReview = (reviewData) => {
  return api.post('/reviews', reviewData);
};

export const getReviewById = (id) => {
  return api.get(`/reviews/${id}`);
};

export const updateReview = (id, reviewData) => {
  return api.patch(`/reviews/${id}`, reviewData);
};

export const deleteReview = (id, token) => {
  return api.delete(`/reviews/${id}`, {
    data: { token }
  });
};

export const getAllReviews = (token) => {
  return api.get(`/reviews/getAllReviews/${token}`);
};