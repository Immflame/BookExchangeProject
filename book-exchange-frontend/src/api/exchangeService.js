import api from './apiService';

export const getMyExchanges = () => {
  return api.get('/exchanges/my_exchanges');
};

export const updateExchangeStatus = (id, status) => {
  return api.patch(`/exchanges/${id}`, null, {
    params: { status }
  });
};

export const createExchange = (exchangeData) => {
  return api.post('/exchanges', exchangeData);
};

export const getAllExchanges = () => {
  return api.get('/exchanges/get_all_exchanges');
};