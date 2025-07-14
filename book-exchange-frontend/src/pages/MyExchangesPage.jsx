import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Box,
  CircularProgress,
  Card,
  CardContent,
  CardMedia,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  Chip
} from '@mui/material';
import { getMyExchanges, updateExchangeStatus } from '../api/exchangeService';
import { getGenreDisplayName } from '../utils/genreHelper';
import { useNavigate } from 'react-router-dom';
import { getToken, getUserInfo } from '../utils/auth';

const MyExchangesPage = () => {
  const [exchanges, setExchanges] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedExchange, setSelectedExchange] = useState(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [actionError, setActionError] = useState('');
  const navigate = useNavigate();

  const token = getToken();
  const userInfo = getUserInfo();

  useEffect(() => {
    const fetchExchanges = async () => {
      try {
        setLoading(true);
        const response = await getMyExchanges(token);
        setExchanges(response.data);
      } catch (err) {
        console.error('Ошибка загрузки предложений обмена:', err);
        setError('Не удалось загрузить предложения обмена');
      } finally {
        setLoading(false);
      }
    };

    fetchExchanges();
  }, [token]);

  const handleOpenDialog = (exchange) => {
    setSelectedExchange(exchange);
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setSelectedExchange(null);
    setActionError('');
  };

  const handleUpdateStatus = async (status) => {
    if (!selectedExchange) return;

    setActionLoading(true);
    setActionError('');

    try {
      const response = await updateExchangeStatus(selectedExchange.id, status);
      
      const updatedExchangeFromServer = response.data;
      console.log('Ответ сервера при обновлении статуса:', response.data);
      
      const updatedExchanges = exchanges.map(ex =>
        ex.id === selectedExchange.id ? updatedExchangeFromServer : ex
      );
      
      setExchanges(updatedExchanges);
      handleCloseDialog();
    } catch (err) {
      console.error('Ошибка обновления статуса обмена:', err);
      setActionError(err.response?.data?.message || 'Ошибка обновления статуса');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <Container maxWidth="lg">
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  const truncateTitle = (title, maxLength = 14) => {
    return title.length > maxLength ? title.slice(0, maxLength) + '...' : title;
  };

  const incomingExchanges = exchanges.filter(
    exchange => exchange.user2.id === userInfo?.userId
  );

  const outgoingExchanges = exchanges.filter(
    exchange => exchange.user1.id === userInfo?.userId
  );

  return (
    <Container maxWidth="lg">
      <Typography variant="h4" sx={{ mt: 3, mb: 3 }}>
        Предложения обмена
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {incomingExchanges.length === 0 ? (
        <Typography variant="body1">
          У вас нет активных предложений обмена.
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
          {incomingExchanges.map((exchange) => (
            <Card
              key={exchange.id}
              sx={{ height: '100%', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}
            >
              <CardMedia
                component="img"
                height="170"
                image="/images/book_image.jpg"
                alt={exchange.book1.title}
                sx={{ objectFit: 'cover' }}
              />
              <CardContent>
                <Typography gutterBottom variant="h6" component="div" noWrap>
                  {truncateTitle(exchange.book1.title)}
                </Typography>
                <Typography variant="body2" color="text.secondary" noWrap>
                  Предложил: {exchange.user1.username}
                </Typography>
                <Chip
                  label={
                    exchange.status === 'PENDING' ? 'Ожидание'
                      : exchange.status === 'COMPLETED' ? 'Завершен'
                        : 'Отменен'
                  }
                  color={
                    exchange.status === 'PENDING' ? 'primary'
                      : exchange.status === 'COMPLETED' ? 'success'
                        : 'error'
                  }
                  sx={{ mt: 1 }}
                />
              </CardContent>
              <Box sx={{ p: 2, mt: 'auto' }}>
                <Button
                  variant="outlined"
                  fullWidth
                  onClick={() => handleOpenDialog(exchange)}
                >
                  Подробнее
                </Button>
              </Box>
            </Card>
          ))}
        </Box>
      )}

      <Typography variant="h4" sx={{ mt: 5, mb: 3 }}>
        Мои предложения обмена
      </Typography>

      {outgoingExchanges.length === 0 ? (
        <Typography variant="body1">
          У вас нет активных предложений обмена.
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
          {outgoingExchanges.map((exchange) => (
            <Card
              key={exchange.id}
              sx={{ height: '100%', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}
            >
              <CardMedia
                component="img"
                height="170"
                image="/images/book_image.jpg"
                alt={exchange.book1.title}
                sx={{ objectFit: 'cover' }}
              />
              <CardContent>
                <Typography gutterBottom variant="h6" component="div" noWrap>
                  {truncateTitle(exchange.book1.title)}
                </Typography>
                <Typography variant="body2" color="text.secondary" noWrap>
                  Предложил: {exchange.user1.username}
                </Typography>
                <Chip
                  label={
                    exchange.status === 'PENDING' ? 'Ожидание'
                      : exchange.status === 'COMPLETED' ? 'Завершен'
                        : 'Отменен'
                  }
                  color={
                    exchange.status === 'PENDING' ? 'primary'
                      : exchange.status === 'COMPLETED' ? 'success'
                        : 'error'
                  }
                  sx={{ mt: 1 }}
                />
              </CardContent>
              <Box sx={{ p: 2, mt: 'auto' }}>
                <Button
                  variant="outlined"
                  fullWidth
                  onClick={() => handleOpenDialog(exchange)}
                >
                  Подробнее
                </Button>
              </Box>
            </Card>
          ))}
        </Box>
      )}

      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="md">
        {selectedExchange && (
          <>
            <DialogTitle>Детали предложения обмена</DialogTitle>
            <DialogContent>
              {actionError && (
                <Alert severity="error" sx={{ mb: 2 }}>
                  {actionError}
                </Alert>
              )}

              <Typography variant="h6" sx={{ mt: 2 }}>
                Книга, которую предлагают:
              </Typography>
              <Card sx={{ mb: 2 }}>
                <CardContent>
                  <Typography variant="body1">
                    <strong>Название:</strong> {selectedExchange.book1.title}
                  </Typography>
                  <Typography variant="body1">
                    <strong>Автор:</strong> {selectedExchange.book1.author}
                  </Typography>
                  <Typography variant="body1">
                    <strong>Жанр:</strong> {getGenreDisplayName(selectedExchange.book1.genre)}
                  </Typography>
                  <Typography variant="body1">
                    <strong>Локация:</strong> {selectedExchange.book1.location?.name || 'Не указана'}
                  </Typography>
                  <Typography variant="body1">
                    <strong>Владелец:</strong> {selectedExchange.user1.username}
                  </Typography>
                </CardContent>
              </Card>

              <Typography variant="h6" sx={{ mt: 2 }}>
                Ваша книга для обмена:
              </Typography>
              <Card>
                <CardContent>
                  <Typography variant="body1">
                    <strong>Название:</strong> {selectedExchange.book2.title}
                  </Typography>
                  <Typography variant="body1">
                    <strong>Автор:</strong> {selectedExchange.book2.author}
                  </Typography>
                  <Typography variant="body1">
                    <strong>Жанр:</strong> {getGenreDisplayName(selectedExchange.book2.genre)}
                  </Typography>
                  <Typography variant="body1">
                    <strong>Локация:</strong> {selectedExchange.book2.location?.name || 'Не указана'}
                  </Typography>
                </CardContent>
              </Card>

              <Box sx={{ mt: 2 }}>
                <Typography variant="body1">
                  <strong>Статус:</strong> {selectedExchange.status === 'PENDING' ? 'Ожидание' : selectedExchange.status === 'COMPLETED' ? 'Завершен' : 'Отменен'}
                </Typography>
                <Typography variant="body1">
                  <strong>Дата предложения:</strong> {new Date(selectedExchange.exchangeDate).toLocaleString()}
                </Typography>
              </Box>
            </DialogContent>
            <DialogActions sx={{ justifyContent: 'space-between', p: 3 }}>
              {selectedExchange.status === 'PENDING' && (
                <>
                  <Button
                    variant="contained"
                    color="error"
                    onClick={() => handleUpdateStatus('CANCELLED')}
                    disabled={actionLoading}
                  >
                    {actionLoading ? <CircularProgress size={24} /> : 'Отменить'}
                  </Button>
                  {selectedExchange.user2.id === userInfo?.userId && (
                    <Button
                      variant="contained"
                      color="success"
                      onClick={() => handleUpdateStatus('COMPLETED')}
                      disabled={actionLoading}
                    >
                      {actionLoading ? <CircularProgress size={24} /> : 'Обмен состоялся'}
                    </Button>
                  )}
                </>
              )}
              <Button
                variant="outlined"
                onClick={handleCloseDialog}
                disabled={actionLoading}
              >
                Закрыть
              </Button>
            </DialogActions>
          </>
        )}
      </Dialog>
    </Container>
  );
};

export default MyExchangesPage;