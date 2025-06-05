import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Box,
  Paper,
  CircularProgress,
  Alert,
  Tabs,
  Tab,
  Card,
  CardContent,
  CardMedia,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  CircularProgress as MuiCircularProgress
} from '@mui/material';
import { getCurrentUser, updateUser, deleteUser } from '../api/userService';
import { getMyBooks, deleteBook, updateBook, getBookLocations } from '../api/bookService';
import { useNavigate } from 'react-router-dom';
import { getToken } from '../utils/auth';
import { genreOptions } from '../utils/genreHelper';

const ProfilePage = () => {
  const [userData, setUserData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [editMode, setEditMode] = useState(false);
  const [formData, setFormData] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deleteError, setDeleteError] = useState('');
  const navigate = useNavigate();

  const [userBooks, setUserBooks] = useState([]);
  const [booksLoading, setBooksLoading] = useState(false);
  const [booksError, setBooksError] = useState('');

  const [bookDialogOpen, setBookDialogOpen] = useState(false);
  const [selectedBook, setSelectedBook] = useState(null);

  const [editBookDialogOpen, setEditBookDialogOpen] = useState(false);
  const [editBookData, setEditBookData] = useState({
    title: '',
    author: '',
    description: '',
    genre: '',
    locationId: ''
  });
  const [locations, setLocations] = useState([]);
  const [editBookLoading, setEditBookLoading] = useState(false);
  const [editBookError, setEditBookError] = useState('');

  const token = getToken();

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        setLoading(true);
        const response = await getCurrentUser();
        setUserData(response.data);
        setFormData({
          username: response.data.username,
          password: ''
        });
      } catch (err) {
        console.error('Ошибка загрузки данных пользователя:', err);
        setError('Не удалось загрузить данные пользователя');
      } finally {
        setLoading(false);
      }
    };
        fetchUserData();
  }, []);

  useEffect(() => {
    const fetchUserBooks = async () => {
      try {
        setBooksLoading(true);
        setBooksError('');
        const response = await getMyBooks(token);
        setUserBooks(response.data);
      } catch (err) {
        console.error('Ошибка загрузки книг пользователя:', err);
        setBooksError('Не удалось загрузить ваши книги');
      } finally {
        setBooksLoading(false);
      }
    };

    if (userData) {
      fetchUserBooks();
    }
  }, [userData, token]);

  useEffect(() => {
    const fetchLocations = async () => {
      try {
        const response = await getBookLocations();
        setLocations(response.data);
      } catch (err) {
        console.error('Ошибка загрузки локаций:', err);
      }
    };
    fetchLocations();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess(false);

    try {
      const response = await updateUser({
        token,
        username: formData.username,
        password: formData.password
      });

      localStorage.setItem('token', response.data.token);
      setSuccess(true);

      setUserData({
        ...userData,
        username: formData.username
      });

      setEditMode(false);
    } catch (err) {
      console.error('Ошибка обновления данных:', err);
      setError(err.response?.data?.message || 'Ошибка обновления данных');
    }
  };

  const handleOpenDeleteDialog = () => {
    setDeleteDialogOpen(true);
    setDeleteError('');
  };

  const handleCloseDeleteDialog = () => {
    setDeleteDialogOpen(false);
  };

  const handleDeleteAccount = async () => {
    try {
      await deleteUser({ token });
      localStorage.removeItem('token');
      navigate('/auth');
    } catch (err) {
      console.error('Ошибка удаления аккаунта:', err);
      setDeleteError('Не удалось удалить аккаунт');
    } finally {
      setDeleteDialogOpen(false);
    }
  };

  const handleBookClick = (book) => {
    setSelectedBook(book);
    setBookDialogOpen(true);
  };
  const handleCloseBookDialog = () => {
    setBookDialogOpen(false);
    setSelectedBook(null);
  };

  const handleDeleteBook = async () => {
    if (!selectedBook) return;
    try {
      await deleteBook(selectedBook.id, token);
      setUserBooks(userBooks.filter(b => b.id !== selectedBook.id));
      handleCloseBookDialog();
    } catch (err) {
      alert(err.response?.data?.message || 'Ошибка удаления книги');
    }
  };

  const handleOpenEditBookDialog = () => {
    if (!selectedBook) return;
    setEditBookData({
      title: selectedBook.title,
      author: selectedBook.author,
      description: selectedBook.description || '',
      genre: selectedBook.genre || '',
      locationId: selectedBook.location?.id || ''
    });
    setEditBookError('');
    setEditBookDialogOpen(true);
  };

  const handleCloseEditBookDialog = () => {
    setEditBookDialogOpen(false);
  };

  const handleEditBookChange = (e) => {
    const { name, value } = e.target;
    setEditBookData(prev => ({ ...prev, [name]: value }));
  };

  const handleEditBookSubmit = async (e) => {
    e.preventDefault();
    setEditBookError('');
    setEditBookLoading(true);

    try {
      const data = {
        token,
        title: editBookData.title.trim(),
        author: editBookData.author.trim(),
        description: editBookData.description.trim(),
        genre: editBookData.genre,
        locationId: Number(editBookData.locationId)
      };
      const response = await updateBook(selectedBook.id, data);
      setUserBooks(userBooks.map(b => (b.id === selectedBook.id ? response.data : b)));
      setSelectedBook(response.data);
      setEditBookDialogOpen(false);
      setBookDialogOpen(false);
    } catch (err) {
      console.error('Ошибка обновления книги:', err);
      setEditBookError(err.response?.data?.message || 'Ошибка обновления книги');
    } finally {
      setEditBookLoading(false);
    }
  };

  if (loading) {
    return (
      <Container maxWidth="md">
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="md">
      <Typography variant="h4" sx={{ mt: 3, mb: 3 }}>
        Профиль пользователя
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 2 }}>
          Данные успешно обновлены!
        </Alert>
      )}

      <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
        {!editMode ? (
          <>
            <Typography variant="h6" gutterBottom>
              Основная информация
            </Typography>

            <Box sx={{ mb: 2 }}>
              <Typography variant="body1">
                <strong>ID пользователя:</strong> {userData?.id}
              </Typography>
              <Typography variant="body1">
                <strong>Имя пользователя:</strong> {userData?.username}
              </Typography>
              <Typography variant="body1">
                <strong>Роль:</strong> {userData?.role}
              </Typography>
            </Box>

            <Box sx={{ display: 'flex', gap: 2 }}>
              <Button variant="contained" color="primary" onClick={() => setEditMode(true)}>
                Изменить пользовательские данные
              </Button>

              <Button
                variant="outlined"
                sx={{
                  borderColor: 'error.main',
                  color: 'error.main',
                  '&:hover': {
                    borderColor: 'error.dark',
                    backgroundColor: 'rgba(244, 67, 54, 0.04)'
                  }
                }}
                onClick={handleOpenDeleteDialog}
              >
                Удалить аккаунт
              </Button>
            </Box>
          </>
        ) : (
          <form onSubmit={handleSubmit}>
            <Typography variant="h6" gutterBottom>
              Редактирование профиля
            </Typography>

            <TextField
              label="Новое имя пользователя"
              name="username"
              value={formData.username}
              onChange={handleChange}
              fullWidth
              margin="normal"
              required
            />

            <TextField
              label="Новый пароль"
              name="password"
              type="password"
              value={formData.password}
              onChange={handleChange}
              fullWidth
              margin="normal"
              helperText="Оставьте пустым, если не хотите менять пароль"
            />

            <Box sx={{ mt: 2, display: 'flex', gap: 2 }}>
              <Button type="submit" variant="contained" color="primary">
                Сохранить изменения
              </Button>

              <Button variant="outlined" color="secondary" onClick={() => setEditMode(false)}>
                Отмена
              </Button>
            </Box>
          </form>
        )}
      </Paper>

      <Typography variant="h5" sx={{ mb: 2 }}>
        Мои книги
      </Typography>

      {booksLoading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
          <CircularProgress />
        </Box>
      ) : booksError ? (
        <Alert severity="error">{booksError}</Alert>
      ) : userBooks.length === 0 ? (
        <Paper elevation={3} sx={{ p: 3, textAlign: 'center' }}>
          <Typography variant="body1">У вас пока нет добавленных книг.</Typography>
          <Button variant="contained" color="primary" sx={{ mt: 2 }} onClick={() => navigate('/add-book')}>
            Добавить книгу
          </Button>
        </Paper>
      ) : (
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: 'repeat(5, 1fr)',
            columnGap: '16px',
            rowGap: '16px'
          }}
        >
          {userBooks.map((book) => (
            <Card
              key={book.id}
              sx={{ height: '100%', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}
            >
              <CardMedia
                component="img"
                height="140"
                image="/images/book_image.jpg"
                alt={book.title}
                sx={{ objectFit: 'cover' }}
              />
              <CardContent sx={{ flexGrow: 1 }}>
                <Typography gutterBottom variant="h6" component="div" noWrap>
                  {book.title}
                </Typography>
                <Typography variant="body2" color="text.secondary" noWrap>
                  Автор: {book.author}
                </Typography>
                <Typography variant="body2" color="text.secondary" noWrap>
                  Статус:{' '}
                  {book.status === 'AVAILABLE'
                    ? 'Доступна'
                    : book.status === 'IN_EXCHANGE'
                    ? 'В обмене'
                    : 'Обменена'}
                </Typography>
              </CardContent>
              <Box sx={{ p: 2 }}>
                <Button variant="outlined" fullWidth onClick={() => handleBookClick(book)}>
                  Подробнее
                </Button>
              </Box>
            </Card>
          ))}
        </Box>
      )}

      <Dialog open={bookDialogOpen} onClose={handleCloseBookDialog} maxWidth="md" fullWidth>
        {selectedBook && (
          <>
            <DialogTitle>{selectedBook.title}</DialogTitle>
            <DialogContent dividers>
              <Typography variant="subtitle1" gutterBottom>
                Автор: {selectedBook.author}
              </Typography>
              <Typography variant="body1" paragraph>
                Описание: {selectedBook.description || 'Описание отсутствует'}
              </Typography>
              <Typography variant="body2" color="text.secondary" paragraph>
                Жанр: {selectedBook.genre}
              </Typography>
              <Typography variant="body2" color="text.secondary" paragraph>
                Локация: {selectedBook.location?.name || 'Не указана'}
              </Typography>
            </DialogContent>
            <DialogActions>
              <Button color="error" variant="outlined" onClick={handleDeleteBook}>
                Удалить
              </Button>
              <Button variant="outlined" onClick={handleOpenEditBookDialog}>
                Изменить
              </Button>
              <Button variant="outlined" onClick={handleCloseBookDialog}>
                Закрыть
              </Button>
            </DialogActions>
          </>
        )}
      </Dialog>

      <Dialog open={editBookDialogOpen} onClose={handleCloseEditBookDialog} maxWidth="sm" fullWidth>
        <DialogTitle>Редактирование книги</DialogTitle>
        <DialogContent>
          {editBookError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {editBookError}
            </Alert>
          )}
          <Box component="form" onSubmit={handleEditBookSubmit} noValidate>
            <TextField
              label="Название книги"
              name="title"
              value={editBookData.title}
              onChange={handleEditBookChange}
              fullWidth
              margin="normal"
              required
            />
            <TextField
              label="Автор"
              name="author"
              value={editBookData.author}
              onChange={handleEditBookChange}
              fullWidth
              margin="normal"
              required
            />
            <TextField
              label="Описание"
              name="description"
              value={editBookData.description}
              onChange={handleEditBookChange}
              fullWidth
              margin="normal"
              multiline
              rows={4}
            />
            <FormControl fullWidth margin="normal" required>
              <InputLabel>Жанр</InputLabel>
              <Select name="genre" value={editBookData.genre} onChange={handleEditBookChange} label="Жанр">
                <MenuItem value="" disabled>
                  Выберите жанр
                </MenuItem>
                {genreOptions.map((genre) => (
                  <MenuItem key={genre.value} value={genre.value}>
                    {genre.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <FormControl fullWidth margin="normal" required>
              <InputLabel>Локация обмена</InputLabel>
              <Select
                name="locationId"
                value={editBookData.locationId}
                onChange={handleEditBookChange}
                label="Локация обмена"
              >
                <MenuItem value="" disabled>
                  Выберите локацию
                </MenuItem>
                {locations.map((location) => (
                  <MenuItem key={location.id} value={location.id}>
                    {location.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseEditBookDialog} variant="outlined" disabled={editBookLoading}>
            Отмена
          </Button>
          <Button
            onClick={handleEditBookSubmit}
            variant="contained"
            color="primary"
            disabled={editBookLoading}
          >
            {editBookLoading ? <MuiCircularProgress size={24} /> : 'Подтвердить'}
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={deleteDialogOpen} onClose={handleCloseDeleteDialog}>
        <DialogTitle>Подтверждение удаления аккаунта</DialogTitle>
        <DialogContent>
          <Typography>
            Вы действительно хотите удалить аккаунт? Это действие невозможно отменить.
          </Typography>
          {deleteError && (
            <Alert severity="error" sx={{ mt: 2 }}>
              {deleteError}
            </Alert>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog} variant="outlined">
            Отмена
          </Button>
          <Button onClick={handleDeleteAccount} variant="contained" color="error" autoFocus>
            Да, я хочу удалить аккаунт
          </Button>
        </DialogActions>
        </Dialog>
    </Container>
  );
};

export default ProfilePage;