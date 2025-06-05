import api from './apiService';

export const getAllLocations = () => {
  return api.get('/locations');
};

export const createLocation = (data) => {
  return api.post('/locations', data);
};

export const updateLocation = (id, data) => {
  return api.put(`/locations/${id}`, data);
};

export const deleteLocation = (id, token) => {
  return api.delete(`/locations/${id}`, {
    data: { token }
  });
};
