import React, { useState, useEffect } from 'react';
import { 
  AppBar, 
  Toolbar, 
  Button, 
  Box, 
  IconButton,
  TextField,
  Alert,
  CircularProgress
} from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom';
import { logout, getUserInfo } from '../utils/auth';
import axios from 'axios';

const Navbar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [activeButton, setActiveButton] = useState('home');
  const [userRole, setUserRole] = useState('user');
  const [searchUsername, setSearchUsername] = useState('');
  const [searchError, setSearchError] = useState('');
  const [searchLoading, setSearchLoading] = useState(false);

  useEffect(() => {
    const userInfo = getUserInfo();
    if (userInfo && userInfo.role) {
      setUserRole(userInfo.role);
    }
  }, []);

  useEffect(() => {
    if (location.pathname === '/') setActiveButton('home');
    if (location.pathname === '/about') setActiveButton('about');
    if (location.pathname === '/profile') setActiveButton('profile');
    if (location.pathname === '/my-exchanges') setActiveButton('exchanges');
    if (location.pathname === '/admin') setActiveButton('admin');
  }, [location]);

  const handleSearchUser = async () => {
    if (!searchUsername.trim()) return;
    
    setSearchError('');
    setSearchLoading(true);
    
    try {
      const response = await axios.get(`http://localhost:8080/users/get_user_by_username/${searchUsername.trim()}`);
      const user = response.data;
      
      navigate(`/user/${user.id}`);
      setSearchUsername('');
    } catch (err) {
      if (err.response && err.response.status === 404) {
        setSearchError('Пользователь не найден');
      } else {
        setSearchError('Ошибка сервера');
      }
    } finally {
      setSearchLoading(false);
    }
  };

  const handleAdminPanel = () => {
    navigate('/admin');
  };

  return (
    <AppBar position="fixed" color="default" elevation={1} sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
      <Toolbar>
        <img 
          src="/images/logo_image.png" 
          alt="Логотип BookExchange" 
          style={{ 
            width: '40px', 
            height: '40px',
            borderRadius: '4px',
            objectFit: 'cover',
            marginRight: '16px'
          }} 
        />
        <Box sx={{ flexGrow: 1, display: 'flex', gap: 1 }}>
          <Button
            variant={activeButton === 'home' ? 'contained' : 'outlined'}
            onClick={() => navigate('/')}
          >
            Главная
          </Button>
          <Button
            variant={activeButton === 'about' ? 'contained' : 'outlined'}
            onClick={() => navigate('/about')}
          >
            О сервисе
          </Button>
          <Button
            variant={activeButton === 'exchanges' ? 'contained' : 'outlined'}
            onClick={() => navigate('/my-exchanges')}
          >
            Предложения обмена
          </Button>
          <Button
            variant={activeButton === 'profile' ? 'contained' : 'outlined'}
            onClick={() => navigate('/profile')}
          >
            Профиль
          </Button>
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center', mr: 2 }}>
          <TextField
            size="small"
            placeholder="Найти пользователя"
            value={searchUsername}
            onChange={(e) => setSearchUsername(e.target.value)}
            error={!!searchError}
            helperText={searchError}
            onKeyPress={(e) => {
              if (e.key === 'Enter') {
                handleSearchUser();
              }
            }}
            sx={{
              minWidth: 200,
              '& .MuiOutlinedInput-root': {
                '& fieldset': {
                  borderColor: 'rgba(0, 0, 0, 0.23)',
                },
                '&:hover fieldset': {
                  borderColor: 'rgba(0, 0, 0, 0.5)',
                },
                '&.Mui-focused fieldset': {
                  borderColor: 'primary.main',
                },
              },
            }}
          />
          <Button 
            variant="outlined" 
            sx={{ ml: 1 }}
            onClick={handleSearchUser}
            disabled={searchLoading}
          >
            {searchLoading ? <CircularProgress size={20} /> : 'Найти'}
          </Button>
        </Box>
        {userRole === 'admin' && (
          <Button
            variant={activeButton === 'admin' ? 'contained' : 'outlined'}
            color="secondary"
            onClick={handleAdminPanel}
            sx={{ mr: 2 }}
          >
            Админ панель
          </Button>
        )}

        <Button 
          variant="outlined" 
          color="error" 
          onClick={logout}
        >
          Выйти
        </Button>
      </Toolbar>
    </AppBar>
  );
};

export default Navbar;