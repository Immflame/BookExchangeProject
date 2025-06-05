import api from './apiService';

export const register = (credentials) => {
  return api.post('/users/register', credentials);
};

export const login = (credentials) => {
  return api.post('/users/login', credentials);
};

export const getCurrentUser = () => {
  return api.post('/users/me', { token: localStorage.getItem('token') });
};

export const updateUser = (data) => {
  return api.put('/users/', data);
};

export const deleteUser = (data) => {
  return api.delete('/users/', { 
    data: { token: data.token } 
  });
};

export const getUserById = (id) => {
  return api.get(`/users/${id}`);
};

export const getUserByUsername = (username) => {
  return api.get(`/users/get_user_by_username/${username}`);
};