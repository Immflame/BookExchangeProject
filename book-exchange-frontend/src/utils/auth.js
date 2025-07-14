import { jwtDecode } from 'jwt-decode';

export const authEvent = new Event('authChange');

export const setToken = (token) => {
  localStorage.setItem('token', token);
  
  try {
    const decoded = jwtDecode(token);
    localStorage.setItem('userInfo', JSON.stringify({
      id: decoded.user_id,
      username: decoded.username,
      role: decoded.role
    }));
  } catch (error) {
    console.error('Ошибка декодирования токена:', error);
  }
  
  window.dispatchEvent(authEvent);
};

export const getToken = () => {
  return localStorage.getItem('token');
};

export const getAuthHeader = () => {
  const token = getToken();
  return token ? `Bearer ${token}` : '';
};

export const getUserInfo = () => {
  const token = getToken();
  if (!token) return null;
  
  try {
    const decoded = jwtDecode(token);
    return {
      userId: decoded.user_id,
      username: decoded.username,
      role: decoded.role
    };
  } catch (error) {
    console.error('Ошибка декодирования токена:', error);
    return null;
  }
};

export const isAuthenticated = () => {
  return !!getToken();
};

export const logout = () => {
  localStorage.removeItem('token');
  window.dispatchEvent(authEvent);
};
