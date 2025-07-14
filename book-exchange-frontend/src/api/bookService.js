import api from './apiService';

export const getBooks = (filters = {}) => {
  const params = {
    genres: filters.genres?.join(','),
    locationIds: filters.locationIds?.join(',')
  };
  
  return api.get('/books', { params });
};

export const getBookById = (id) => {
  return api.get(`/books/${id}`);
};

export const createBook = (bookData) => {
  return api.post('/books', bookData);
};

export const updateBook = (id, bookData) => {
  return api.put(`/books/${id}`, bookData);
};

export const deleteBook = (id) => {
  return api.delete(`/books/${id}`);
};

export const getUserBooks = () => {
  return api.get('/books/my_books');
};

export const getBookLocations = () => {
  return api.get('/locations');
};

export const getBookDetails = (id) => {
  return api.get(`/books/${id}`);
};

export const getMyBooks = () => {
  return api.get('/books/my_books');
};

export const getBooksByUserId = (userId) => {
  return api.get(`/books/user/${userId}`);
};

