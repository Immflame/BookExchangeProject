import React, { useState } from 'react';
import { Box, Button, TextField, Typography, Paper, CircularProgress } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { login, register } from '../api/userService';
import { setToken } from '../utils/auth';

const AuthPage = () => {
  const [activeTab, setActiveTab] = useState('login');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    
    try {
      let response;
      if (activeTab === 'login') {
        response = await login({ username, password });
      } else {
        response = await register({ username, password });
      }
      
      if (response.data.token) {
        setToken(response.data.token);
        navigate('/');
      } else {
        setError('Не удалось получить токен');
      }
    } catch (err) {
      let errorMessage = 'Ошибка сервера';
      
      if (err.response) {
        if (err.response.status === 401) {
          errorMessage = 'Неверные учетные данные';
        } else if (err.response.status === 400) {
          errorMessage = 'Некорректный запрос';
        } else if (err.response.status === 409) {
          errorMessage = 'Пользователь уже существует';
        } else {
          errorMessage = err.response.data?.message || errorMessage;
        }
      } else if (err.request) {
        errorMessage = "Сервер не отвечает. Проверьте подключение.";
      }
      
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box 
      sx={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        backgroundColor: '#f5f5f5'
      }}
    >
      <Paper elevation={3} sx={{ padding: 4, width: 400 }}>
        <Box sx={{ display: 'flex', mb: 3 }}>
          <Button
            variant={activeTab === 'login' ? 'contained' : 'outlined'}
            onClick={() => setActiveTab('login')}
            sx={{ flex: 1, mr: 1 }}
          >
            Вход
          </Button>
          <Button
            variant={activeTab === 'register' ? 'contained' : 'outlined'}
            onClick={() => setActiveTab('register')}
            sx={{ flex: 1, ml: 1 }}
          >
            Регистрация
          </Button>
        </Box>
        
        <Typography variant="h5" align="center" mb={3}>
          {activeTab === 'login' ? 'Вход в систему' : 'Регистрация'}
        </Typography>
        
        <form onSubmit={handleSubmit}>
          <TextField
            label="Имя пользователя"
            variant="outlined"
            fullWidth
            margin="normal"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
          <TextField
            label="Пароль"
            variant="outlined"
            type="password"
            fullWidth
            margin="normal"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          
          {error && (
            <Typography color="error" sx={{ mt: 1 }}>
              {error}
            </Typography>
          )}
          
          <Button
            type="submit"
            variant="contained"
            color="primary"
            fullWidth
            sx={{ mt: 3 }}
            disabled={loading}
          >
            {loading ? (
              <CircularProgress size={24} color="inherit" />
            ) : activeTab === 'login' ? (
              'Войти'
            ) : (
              'Зарегистрироваться'
            )}
          </Button>
        </form>
      </Paper>
    </Box>
  );
};

export default AuthPage;