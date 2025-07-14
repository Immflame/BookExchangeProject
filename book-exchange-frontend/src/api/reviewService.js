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

export const deleteReview = (id) => {
  return api.delete(`/reviews/${id}`);
};

export const getAllReviews = () => {
  return api.get('/reviews/getAllReviews');
};