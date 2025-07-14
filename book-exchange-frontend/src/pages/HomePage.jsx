import React, { useState, useEffect } from 'react';
import { 
  Container, 
  Typography, 
  Box, 
  Button,
  CircularProgress,
  Card,
  CardContent,
  CardMedia,
  Alert,
  Grid,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem
} from '@mui/material';
import { Link } from 'react-router-dom';
import BookFilters from '../components/BookFilters';
import { getBooks, getBookLocations, getUserBooks } from '../api/bookService';
import { createExchange } from '../api/exchangeService';
import { getGenreDisplayName } from '../utils/genreHelper';
import { getUserInfo } from '../utils/auth';

const HomePage = () => {
  const [loading, setLoading] = useState(true);
  const [books, setBooks] = useState([]);
  const [locations, setLocations] = useState([]);
  const [error, setError] = useState('');
  const [filters, setFilters] = useState({
    genres: [],
    locationIds: []
  });
  const [appliedFilters, setAppliedFilters] = useState({
    genres: [],
    locationIds: []
  });
  
  const [selectedBook, setSelectedBook] = useState(null);
  const [bookDialogOpen, setBookDialogOpen] = useState(false);
  
  const [userBooks, setUserBooks] = useState([]);
  const [selectedUserBook, setSelectedUserBook] = useState('');
  const [exchangeLoading, setExchangeLoading] = useState(false);
  const [exchangeError, setExchangeError] = useState('');
  const [exchangeSuccess, setExchangeSuccess] = useState(false);
  const userInfo = getUserInfo();

useEffect(() => {
  const fetchData = async () => {
    try {
      setLoading(true);

      const locationsResponse = await getBookLocations();
      setLocations(locationsResponse.data);

      const booksResponse = await getBooks(appliedFilters);

      const filteredBooks = booksResponse.data.filter(book => book.userId !== userInfo?.userId);

      setBooks(filteredBooks);
    } catch (err) {
      console.error('Ошибка загрузки данных:', err);
      setError('Не удалось загрузить данные');
    } finally {
      setLoading(false);
    }
  };

  fetchData();
}, [appliedFilters, userInfo?.userId]);

    useEffect(() => {
    const fetchUserBooks = async () => {
      if (bookDialogOpen && selectedBook) {
        try {
          setExchangeLoading(true);
          setExchangeError('');
          setExchangeSuccess(false);
          
          // Убран параметр token
          const response = await getUserBooks();
          setUserBooks(response.data);
        } catch (err) {
          console.error('Ошибка загрузки книг пользователя:', err);
          setExchangeError('Не удалось загрузить ваши книги');
        } finally {
          setExchangeLoading(false);
        }
      }
    };
    
    fetchUserBooks();
  }, [bookDialogOpen, selectedBook]);

  const handleApplyFilters = (newFilters) => {
    setAppliedFilters(newFilters);
    setFilters(newFilters);
  };

  const handleBookClick = (book) => {
    setSelectedBook(book);
    setBookDialogOpen(true);
    setSelectedUserBook('');
    setExchangeError('');
    setExchangeSuccess(false);
  };

  const handleCloseBookDialog = () => {
    setBookDialogOpen(false);
    setSelectedBook(null);
    setSelectedUserBook('');
    setExchangeError('');
    setExchangeSuccess(false);
  };

  const handleBookSelect = (e) => {
    setSelectedUserBook(e.target.value);
  };

  const handleProposeExchange = async () => {
    if (!selectedUserBook || !selectedBook) return;
    
    try {
      setExchangeLoading(true);
      setExchangeError('');
      
      await createExchange({
        book1Id: selectedUserBook,
        book2Id: selectedBook.id
      });
      
      setExchangeSuccess(true);
      
      setTimeout(() => {
        handleCloseBookDialog();
      }, 2000);
    } catch (err) {
      console.error('Ошибка предложения обмена:', err);
      setExchangeError(err.response?.data?.message || 'Ошибка предложения обмена');
    } finally {
      setExchangeLoading(false);
    }
  };

  const truncateTitle = (title, maxLength = 14) => {
    return title.length > maxLength ? title.slice(0, maxLength) + '...' : title;
  };

return (
  <Container maxWidth="lg">
    <Box sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Добро пожаловать в BookExchange!
      </Typography>
      
      <Typography variant="body1" paragraph>
        Здесь вы можете обмениваться книгами с другими читателями. Найдите интересующую вас книгу 
        или предложите свои для обмена.
      </Typography>
      
      <Button 
        variant="contained" 
        color="primary" 
        sx={{ mt: 2, mb: 4 }}
        component={Link}
        to="/add-book"
      >
        Добавить книгу
      </Button>
    </Box>
    
    <BookFilters 
      locations={locations} 
      onApplyFilters={handleApplyFilters}
      selectedGenres={filters.genres}
      selectedLocationIds={filters.locationIds}
    />
    
    <Typography variant="h5" gutterBottom>
      Доступные книги
      {books.length > 0 && ` (${books.length})`}
    </Typography>
    
    {error ? (
      <Alert severity="error" sx={{ mb: 3 }}>
        {error}
      </Alert>
    ) : null}
    
    {loading ? (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    ) : books.length === 0 ? (
      <Typography variant="body1" sx={{ mt: 2 }}>
        По вашему запросу книг не найдено. Попробуйте изменить фильтры.
      </Typography>
    ) : (
      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: 'repeat(5, 1fr)',
          columnGap: '16px',
          rowGap: '16px',
        }}
      >
        {books.map((book) => (
          <Card
            key={book.id}
            sx={{
              height: 300,
              display: 'flex',
              flexDirection: 'column',
              overflow: 'hidden'
            }}
          >
            <CardMedia
              component="img"
              height="200"
              image="/images/book_image.jpg"
              alt={book.title}
              sx={{ objectFit: 'cover' }}
            />
            <CardContent sx={{ flexGrow: 1, p: 1 }}>
              <Typography gutterBottom variant="h6" component="div" noWrap>
                {truncateTitle(book.title)}
              </Typography>
            </CardContent>
            <Box sx={{ p: 1 }}>
              <Button
                variant="outlined"
                fullWidth
                onClick={() => handleBookClick(book)}
              >
                Подробнее
              </Button>
            </Box>
          </Card>
        ))}
      </Box>
    )}
    
    <Dialog
      open={bookDialogOpen}
      onClose={handleCloseBookDialog}
      maxWidth="md"
      fullWidth
    >
      {selectedBook && (
        <>
          <DialogTitle>
            <Typography variant="h4" component="div">
              {selectedBook.title}
            </Typography>
            <Typography variant="subtitle1" color="text.secondary">
              {selectedBook.author}
            </Typography>
          </DialogTitle>
          
          <DialogContent dividers>
            {exchangeError && (
              <Alert severity="error" sx={{ mb: 2 }}>
                {exchangeError}
              </Alert>
            )}
            
            {exchangeSuccess && (
              <Alert severity="success" sx={{ mb: 2 }}>
                Обмен успешно предложен!
              </Alert>
            )}
            
            <Grid container spacing={3}>
              <Grid item xs={12} md={5}>
                <Card>
                  <CardMedia
                    component="img"
                    height="300"
                    image="/images/book_image.jpg"
                    alt={selectedBook.title}
                    sx={{ objectFit: 'cover' }}
                  />
                </Card>
              </Grid>
              
              <Grid item xs={12} md={7}>
                <Card variant="outlined" sx={{ mb: 2 }}>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Информация о книге
                                        </Typography>
                    
                    <Typography variant="body1" paragraph>
                      <strong>Владелец:</strong> {selectedBook.username}
                    </Typography>
                    
                    <Typography variant="body1" paragraph>
                      <strong>Жанр:</strong> {getGenreDisplayName(selectedBook.genre)}
                    </Typography>
                    
                    <Typography variant="body1" paragraph>
                      <strong>Локация:</strong> {selectedBook.location?.name || 'Не указана'}
                    </Typography>
                    
                    <Typography variant="body1" paragraph>
                      <strong>Статус:</strong> {selectedBook.status === 'AVAILABLE' 
                        ? 'Доступна' 
                        : selectedBook.status === 'IN_EXCHANGE' 
                          ? 'В процессе обмена' 
                          : 'Обменена'}
                    </Typography>
                    
                    <Typography variant="body1">
                      <strong>Описание:</strong>
                    </Typography>
                    <Typography variant="body2" color="text.secondary" paragraph>
                      {selectedBook.description || 'Описание отсутствует'}
                    </Typography>
                  </CardContent>
                </Card>
                
                <Typography variant="h6" gutterBottom>
                  Предложить обмен
                </Typography>
                {exchangeLoading ? (
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
                      value={selectedUserBook}
                      onChange={handleBookSelect}
                      label="Выберите вашу книгу для обмена"
                      >
                        <MenuItem value="">
                        <em>Выберите книгу</em>
                        </MenuItem>
                        {userBooks.map((book) => (
                          <MenuItem key={book.id} value={book.id}>
                            {truncateTitle(book.title)} - {truncateTitle(book.author)}
                          </MenuItem>
                          ))}
                      </Select>
                    </FormControl>
                    
                    <Button
                      variant="contained"
                      color="primary"
                      fullWidth
                      onClick={handleProposeExchange}
                      disabled={!selectedUserBook || exchangeLoading || exchangeSuccess}
                    >
                      {exchangeLoading ? <CircularProgress size={24} /> : 'Предложить обмен'}
                    </Button>
                  </>
                )}
              </Grid>
            </Grid>
          </DialogContent>
          
          <DialogActions>
            <Button onClick={handleCloseBookDialog} color="primary">
              Закрыть
            </Button>
          </DialogActions>
        </>
      )}
    </Dialog>
  </Container>
)};

export default HomePage;