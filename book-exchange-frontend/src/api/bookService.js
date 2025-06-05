import api from './apiService';

export const getBooks = (filters = {}) => {
  const params = {};
  
  if (filters.genres && filters.genres.length > 0) {
    params.genres = filters.genres.join(',');
  }
  
  if (filters.locationIds && filters.locationIds.length > 0) {
    params.locationIds = filters.locationIds.join(',');
  }
  
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

export const deleteBook = (id, token) => {
  return api.delete(`/books/${id}`, { 
    data: { token } 
  });
};

export const getUserBooks = (token) => {
  return api.get(`/books/my_books/${token}`);
};

export const getBookLocations = () =>{
    return api.get('/locations')
};

export const getBookDetails = (id) => {
  return api.get(`/books/${id}`);
};

export const getMyBooks = (token) => {
  return api.get(`/books/my_books/${token}`);
};

export const getBooksByUserId = (userId) => {
  return api.get(`/books/user/${userId}`);
};

