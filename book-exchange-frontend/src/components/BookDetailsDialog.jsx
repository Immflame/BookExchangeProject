import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Typography,
  Box,
  Button,
  CircularProgress,
  Alert,
  Card,
  CardContent,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
  CardMedia
} from '@mui/material';
import { getUserBooks } from '../api/bookService';
import { createExchange } from '../api/exchangeService';
import { getGenreDisplayName } from '../utils/genreHelper';
import { getToken } from '../utils/auth';

const BookDetailsDialog = ({ open, book, onClose }) => {
  const [loading, setLoading] = useState(false);
  const [userBooks, setUserBooks] = useState([]);
  const [selectedBookId, setSelectedBookId] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    const fetchUserBooks = async () => {
      if (open && book) {
        try {
          setLoading(true);
          setError('');
          const token = getToken();
          const response = await getUserBooks(token);
          setUserBooks(response.data.filter(b => b.status === 'AVAILABLE'));
        } catch (err) {
          console.error('Ошибка загрузки книг пользователя:', err);
          setError('Не удалось загрузить ваши книги');
        } finally {
          setLoading(false);
        }
      }
    };
    
    fetchUserBooks();
  }, [open, book]);

  const fetchUserBooks = async () => {
    try {
      setLoading(true);
      setError('');
      const token = localStorage.getItem('token');
      const response = await getUserBooks(token);
      setUserBooks(response.data);
    } catch (err) {
      console.error('Ошибка загрузки книг пользователя:', err);
      setError('Не удалось загрузить ваши книги');
    } finally {
      setLoading(false);
    }
  };

  const handleBookSelect = (event) => {
    setSelectedBookId(event.target.value);
  };

  const handleProposeExchange = async () => {
    if (!selectedBookId) return;

    try {
      setLoading(true);
      setError('');
      const token = localStorage.getItem('token');
      
      await createExchange({
        token,
        book1Id: selectedBookId,
        book2Id: book.id
      });
      setSuccess(true);
    } catch (err) {
      console.error('Ошибка предложения обмена:', err);
      setError(err.response?.data?.message || 'Ошибка предложения обмена');
    } finally {
      setLoading(false);
    }
  };

  if (!book) return null;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Typography variant="h5" component="div">
          {book.title}
        </Typography>
        <Typography variant="subtitle1" color="text.secondary">
          {book.author}
        </Typography>
      </DialogTitle>
      
      <DialogContent dividers>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        
        {success && (
          <Alert severity="success" sx={{ mb: 2 }}>
            Обмен успешно предложен!
          </Alert>
        )}
        
        <Grid container spacing={3}>
          <Grid item xs={12} md={4}>
            <Card>
              <CardMedia
                component="img"
                height="300"
                image="/images/book_image.jpg"
                alt={book.title}
                sx={{ objectFit: 'cover' }}
              />
            </Card>
          </Grid>
          
          <Grid item xs={12} md={8}>
            <Card variant="outlined" sx={{ mb: 2 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Информация о книге
                </Typography>
                
                <Typography variant="body1" paragraph>
                  <strong>Жанр:</strong> {getGenreDisplayName(book.genre)}
                </Typography>
                
                <Typography variant="body1" paragraph>
                  <strong>Локация:</strong> {book.location?.name || 'Не указана'}
                </Typography>
                
                <Typography variant="body1" paragraph>
                  <strong>Владелец:</strong> {book.username}
                </Typography>
                
                <Typography variant="body1">
                  <strong>Описание:</strong>
                </Typography>
                <Typography variant="body2" color="text.secondary" paragraph>
                  {book.description || 'Описание отсутствует'}
                </Typography>
              </CardContent>
            </Card>
            
            <Typography variant="h6" gutterBottom>
              Предложить обмен
            </Typography>
            
            {loading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                <CircularProgress />
              </Box>
            ) : userBooks.length === 0 ? (
              <Typography variant="body1" color="text.secondary">
                У вас нет книг для обмена. Добавьте книги в свой профиль.
              </Typography>
            ) : (
              <>
                <FormControl fullWidth variant="outlined" sx={{ mb: 2 }}>
                  <InputLabel>Выберите вашу книгу для обмена</InputLabel>
                  <Select
                    value={selectedBookId}
                    onChange={handleBookSelect}
                    label="Выберите вашу книгу для обмена"
                  >
                    <MenuItem value="">
                      <em>Выберите книгу</em>
                    </MenuItem>
                    {userBooks.map((userBook) => (
                      <MenuItem key={userBook.id} value={userBook.id}>
                        {userBook.title} - {userBook.author}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
                
                <Button
                  variant="contained"
                  color="primary"
                  fullWidth
                  onClick={handleProposeExchange}
                  disabled={!selectedBookId || loading || success}
                >
                  {loading ? <CircularProgress size={24} /> : 'Предложить обмен'}
                </Button>
              </>
            )}
          </Grid>
        </Grid>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose} color="primary">
          Закрыть
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default BookDetailsDialog;