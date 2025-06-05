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
  Rating,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Grid
} from '@mui/material';
import { useParams, useNavigate } from 'react-router-dom';
import { getUserById } from '../api/userService';
import { getBooksByUserId } from '../api/bookService';
import { getReviewsByRevieweeId } from '../api/reviewService';
import { createReview } from '../api/reviewService';
import { format } from 'date-fns';
import { ru } from 'date-fns/locale';
import BookDetailsDialog from '../components/BookDetailsDialog';
import { getToken, getUserInfo } from '../utils/auth';

const UserProfilePage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [userData, setUserData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState(0);
  const [userBooks, setUserBooks] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [rating, setRating] = useState(null);
  const [booksLoading, setBooksLoading] = useState(false);
  const [reviewsLoading, setReviewsLoading] = useState(false);
  
  const [reviewDialogOpen, setReviewDialogOpen] = useState(false);
  const [reviewRating, setReviewRating] = useState(0);
  const [reviewComment, setReviewComment] = useState('');
  const [reviewLoading, setReviewLoading] = useState(false);
  const [reviewError, setReviewError] = useState('');
  
  const [selectedBook, setSelectedBook] = useState(null);
  const [bookDialogOpen, setBookDialogOpen] = useState(false);
  
  const currentUser = getUserInfo();

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        setLoading(true);
        const response = await getUserById(id);
        setUserData(response.data);
        fetchReviews(response.data.id);
      } catch (err) {
        console.error('Ошибка загрузки данных пользователя:', err);
        setError('Не удалось загрузить данные пользователя');
      } finally {
        setLoading(false);
      }
    };
    
    fetchUserData();
  }, [id]);

  const fetchReviews = async (userId) => {
    try {
      setReviewsLoading(true);
      const response = await getReviewsByRevieweeId(userId);
      const reviewsData = response.data;

      setReviews(reviewsData);

      if (reviewsData.length > 0) {
        const ratings = reviewsData.map(review => review.rating);
        const totalRating = ratings.reduce((sum, rating) => sum + rating, 0);
        const avgRating = totalRating / ratings.length;
        setRating(Math.round(avgRating * 10) / 10);
      } else {
        setRating(null);
      }
    } catch (err) {
      console.error('Ошибка загрузки отзывов:', err);
      setRating(null);
    } finally {
      setReviewsLoading(false);
    }
  };

  useEffect(() => {
    const fetchUserBooks = async () => {
      if (activeTab === 0 && userData && !userBooks.length) {
        try {
          setBooksLoading(true);
          const response = await getBooksByUserId(userData.id);
          setUserBooks(response.data);
        } catch (err) {
          console.error('Ошибка загрузки книг пользователя:', err);
          setError('Не удалось загрузить книги пользователя');
        } finally {
          setBooksLoading(false);
        }
      }
    };
    
    fetchUserBooks();
  }, [activeTab, userData, userBooks.length]);

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };

  const handleOpenReviewDialog = () => {
    setReviewDialogOpen(true);
  };

  const handleCloseReviewDialog = () => {
    setReviewDialogOpen(false);
    setReviewRating(0);
    setReviewComment('');
    setReviewError('');
  };

  const handleSubmitReview = async () => {
    if (!reviewRating) {
      setReviewError('Пожалуйста, поставьте оценку');
      return;
    }

    setReviewLoading(true);
    setReviewError('');

    try {
      const token = getToken();
      await createReview({
        token,
        rating: reviewRating,
        comment: reviewComment,
        revieweeId: userData.id
      });

      fetchReviews(userData.id);
      handleCloseReviewDialog();
    } catch (err) {
      console.error('Ошибка создания отзыва:', err);
      setReviewError(err.response?.data?.message || 'Ошибка создания отзыва');
    } finally {
      setReviewLoading(false);
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

  const truncateTitle = (title, maxLength = 14) => {
    return title.length > maxLength ? title.slice(0, maxLength) + '...' : title;
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
        Профиль пользователя {userData?.username}
      </Typography>
      
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      
      <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
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
          <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
            <Typography variant="body1" sx={{ mr: 1 }}>
              <strong>Рейтинг:</strong>
            </Typography>
            {rating !== null ? (
              <>
                <Rating value={rating} precision={0.1} readOnly />
                <Typography variant="body1" sx={{ ml: 1 }}>
                  ({rating} из 5)
                </Typography>
              </>
            ) : (
              <Typography variant="body1">-</Typography>
            )}
          </Box>
          
          {currentUser?.userId !== userData?.id && (
            <Button 
              variant="contained" 
              sx={{ mt: 2 }}
              onClick={handleOpenReviewDialog}
            >
              Оставить отзыв
            </Button>
          )}
        </Box>
      </Paper>
      
      <Dialog open={reviewDialogOpen} onClose={handleCloseReviewDialog}>
        <DialogTitle>Оставить отзыв</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, minWidth: 400 }}>
            {reviewError && <Alert severity="error">{reviewError}</Alert>}
            <Typography variant="body1">Оценка</Typography>
            <Rating
              value={reviewRating}
              onChange={(event, newValue) => setReviewRating(newValue)}
              size="large"
            />
            <TextField
              label="Комментарий"
              multiline
              rows={4}
              value={reviewComment}
              onChange={(e) => setReviewComment(e.target.value)}
              fullWidth
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseReviewDialog} disabled={reviewLoading}>
            Отменить
          </Button>
          <Button 
            onClick={handleSubmitReview} 
            variant="contained"
            disabled={reviewLoading}
          >
            {reviewLoading ? <CircularProgress size={24} /> : 'Подтвердить'}
          </Button>
        </DialogActions>
      </Dialog>
      
      <Paper elevation={3} sx={{ mb: 3 }}>
        <Tabs value={activeTab} onChange={handleTabChange} variant="fullWidth">
          <Tab label="Книги пользователя" />
          <Tab label="Отзывы о пользователе" />
        </Tabs>
      </Paper>
      
      {activeTab === 0 && (
        <Box>
          {booksLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
              <CircularProgress />
            </Box>
          ) : userBooks.length === 0 ? (
            <Paper elevation={3} sx={{ p: 3, textAlign: 'center' }}>
              <Typography variant="body1">
                У пользователя пока нет книг.
              </Typography>
            </Paper>
          ) : (
            <Box
              sx={{
                display: 'grid',
                gridTemplateColumns: 'repeat(5, 1fr)',
                columnGap: '16px',
                rowGap: '16px',
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
                  <CardContent>
                    <Typography gutterBottom variant="h6" component="div" noWrap>
                      {truncateTitle(book.title)}
                    </Typography>
                    <Typography variant="body2" color="text.secondary" noWrap>
                      Автор: {book.author}
                    </Typography>
                    <Typography variant="body2" color="text.secondary" noWrap>
                      Статус: {book.status === 'AVAILABLE' 
                        ? 'Доступна' 
                        : book.status === 'IN_EXCHANGE' 
                          ? 'В обмене' 
                          : 'Обменена'}
                    </Typography>
                  </CardContent>
                  <Box sx={{ p: 2, mt: 'auto' }}>
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
        </Box>
      )}
      
      {activeTab === 1 && (
        <Box>
          {reviewsLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
              <CircularProgress />
            </Box>
          ) : reviews.length === 0 ? (
            <Paper elevation={3} sx={{ p: 3, textAlign: 'center' }}>
              <Typography variant="body1">
                О пользователе пока нет отзывов.
              </Typography>
            </Paper>
          ) : (
            <Box>
              {reviews.map((review) => (
                <Card variant="outlined" key={review.id} sx={{ mb: 2 }}>
                  <CardContent>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="h6">
                        {review.reviewerUsername}
                      </Typography>
                      <Rating value={review.rating} readOnly />
                    </Box>
                    
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                      {format(new Date(review.createdAt), 'dd MMMM yyyy', { locale: ru })}
                    </Typography>
                    
                    <Typography variant="body1" paragraph>
                      {review.comment || 'Без комментария'}
                    </Typography>
                  </CardContent>
                </Card>
              ))}
            </Box>
          )}
        </Box>
      )}
      
      {selectedBook && (
        <BookDetailsDialog
          open={bookDialogOpen}
          book={selectedBook}
          onClose={handleCloseBookDialog}
        />
      )}
    </Container>
  );
};

export default UserProfilePage;