import api from './apiService';

export const getMyExchanges = (token) => {
  return api.get(`/exchanges/my_exchanges/${token}`);
};

export const updateExchangeStatus = (id, status, token) => {
  return api.patch(`/exchanges/${id}?status=${status}`, { token});
};

export const createExchange = (exchangeData) => {
  return api.post('/exchanges', exchangeData);
};

export const getAllExchanges = (token) => {
  return api.get(`/exchanges/get_all_exchanges/${token}`);
};