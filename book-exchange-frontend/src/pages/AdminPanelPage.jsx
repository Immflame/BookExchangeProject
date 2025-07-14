import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Box,
  Tabs,
  Tab,
  Paper,
  CircularProgress,
  Alert,
  Grid,
  Card,
  CardContent,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  TextField,
  Chip,
  List,
  ListItem,
  ListItemText,
  Divider,
  Rating
} from '@mui/material';
import { getToken } from '../utils/auth';
import { getAllLocations, createLocation, updateLocation, deleteLocation } from '../api/locationService';
import { getAllExchanges } from '../api/exchangeService';
import { getAllReviews } from '../api/reviewService';

const AdminPanelPage = () => {
  const [activeTab, setActiveTab] = useState(0);

  const [locations, setLocations] = useState([]);
  const [loadingLocations, setLoadingLocations] = useState(false);
  const [errorLocations, setErrorLocations] = useState('');

  const [exchanges, setExchanges] = useState([]);
  const [loadingExchanges, setLoadingExchanges] = useState(false);
  const [errorExchanges, setErrorExchanges] = useState('');

  const [reviews, setReviews] = useState([]);
  const [loadingReviews, setLoadingReviews] = useState(false);
  const [errorReviews, setErrorReviews] = useState('');

  const [selectedLocation, setSelectedLocation] = useState(null);
  const [selectedExchange, setSelectedExchange] = useState(null);

  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);

  const [editLocationData, setEditLocationData] = useState({ name: '', description: '' });

  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [createLocationData, setCreateLocationData] = useState({ name: '', description: '' });
  const [createLoading, setCreateLoading] = useState(false);
  const [createError, setCreateError] = useState('');

  const token = getToken();

  useEffect(() => {
    if (activeTab === 0) {
      fetchLocations();
    } else if (activeTab === 1) {
      fetchExchanges();
    } else if (activeTab === 2) {
      fetchReviewsData();
    }
  }, [activeTab]);

  const fetchLocations = async () => {
    setLoadingLocations(true);
        setErrorLocations('');
    try {
      const response = await getAllLocations();
      setLocations(response.data);
    } catch (err) {
      console.error('Ошибка загрузки локаций:', err);
      setErrorLocations('Не удалось загрузить список локаций');
    } finally {
      setLoadingLocations(false);
    }
  };

  const fetchExchanges = async () => {
    setLoadingExchanges(true);
    setErrorExchanges('');
    try {
      const response = await getAllExchanges(token);
      setExchanges(response.data);
    } catch (err) {
      console.error('Ошибка загрузки обменов:', err);
      setErrorExchanges('Не удалось загрузить список обменов');
    } finally {
      setLoadingExchanges(false);
    }
  };

  const fetchReviewsData = async () => {
    setLoadingReviews(true);
    setErrorReviews('');
    try {
      const response = await getAllReviews(token);
      setReviews(response.data);
    } catch (err) {
      console.error('Ошибка загрузки отзывов:', err);
      setErrorReviews('Не удалось загрузить отзывы');
    } finally {
      setLoadingReviews(false);
    }
  };

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
    setSelectedLocation(null);
    setSelectedExchange(null);
    setDetailDialogOpen(false);
    setEditDialogOpen(false);
    setDeleteDialogOpen(false);
    setCreateDialogOpen(false);
  };

  const handleLocationClick = (location) => {
    setSelectedLocation(location);
    setEditLocationData({
      name: location.name,
      description: location.description || ''
    });
    setDetailDialogOpen(true);
  };

  const handleExchangeClick = (exchange) => {
    setSelectedExchange(exchange);
    setDetailDialogOpen(true);
  };

  const handleCloseDetailDialog = () => {
    setDetailDialogOpen(false);
    setSelectedLocation(null);
    setSelectedExchange(null);
  };

  const handleOpenDeleteDialog = () => {
    setDeleteDialogOpen(true);
  };

  const handleCloseDeleteDialog = () => {
    setDeleteDialogOpen(false);
  };

    const handleConfirmDelete = async () => {
    console.log('Удаление локации (заглушка):', selectedLocation.id);
    setLocations(locations.filter(loc => loc.id !== selectedLocation.id));
    setDeleteDialogOpen(false);
    setDetailDialogOpen(false);
    setSelectedLocation(null);
  };

  const handleOpenEditDialog = () => {
    setEditDialogOpen(true);
  };

  const handleCloseEditDialog = () => {
    setEditDialogOpen(false);
  };

  const handleEditChange = (e) => {
    const { name, value } = e.target;
    setEditLocationData(prev => ({ ...prev, [name]: value }));
  };

  const handleSaveLocation = async () => {
    if (!editLocationData.name.trim()) {
      alert('Название локации обязательно');
      return;
    }
    try {
      const data = {
        name: editLocationData.name.trim(),
        description: editLocationData.description.trim()
      };
      
      const response = await updateLocation(selectedLocation.id, data);
      setLocations(locations.map(loc =>
        loc.id === selectedLocation.id ? response.data : loc
      ));
      setEditDialogOpen(false);
      setDetailDialogOpen(false);
      setSelectedLocation(null);
    } catch (err) {
      console.error('Ошибка обновления локации:', err);
      alert(err.response?.data?.message || 'Ошибка обновления локации');
    }
  };

  const handleOpenCreateDialog = () => {
    setCreateLocationData({ name: '', description: '' });
    setCreateError('');
    setCreateDialogOpen(true);
  };

  const handleCloseCreateDialog = () => {
    setCreateDialogOpen(false);
  };

  const handleCreateChange = (e) => {
    const { name, value } = e.target;
    setCreateLocationData(prev => ({ ...prev, [name]: value }));
  };

  const handleCreateLocation = async () => {
    if (!createLocationData.name.trim()) {
      setCreateError('Название локации обязательно');
      return;
    }
    setCreateLoading(true);
    setCreateError('');
    try {
      const data = {
        name: createLocationData.name.trim(),
        description: createLocationData.description.trim()
      };
      
      const response = await createLocation(data);
      setLocations(prev => [...prev, response.data]);
      setCreateDialogOpen(false);
    } catch (err) {
      console.error('Ошибка создания локации:', err);
      setCreateError(err.response?.data?.message || 'Ошибка создания локации');
    } finally {
      setCreateLoading(false);
    }
  };

  const getExchangeStatus = (status) => {
    switch (status) {
      case 'PENDING': return 'Ожидание';
      case 'COMPLETED': return 'Завершен';
      case 'CANCELLED': return 'Отменен';
      default: return status;
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING': return 'primary';
      case 'COMPLETED': return 'success';
      case 'CANCELLED': return 'error';
      default: return 'default';
    }
  };

  return (
    <Container maxWidth="lg">
      <Typography variant="h4" gutterBottom sx={{ mt: 3 }}>
        Админ панель
      </Typography>

      <Paper elevation={3} sx={{ mb: 3 }}>
        <Tabs value={activeTab} onChange={handleTabChange} variant="fullWidth">
          <Tab label="Локации" />
          <Tab label="Все обмены" />
          <Tab label="Все отзывы" />
        </Tabs>
      </Paper>

      {(errorLocations && activeTab === 0) && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {errorLocations}
        </Alert>
      )}
      {(errorExchanges && activeTab === 1) && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {errorExchanges}
        </Alert>
      )}
      {(errorReviews && activeTab === 2) && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {errorReviews}
        </Alert>
      )}

      {activeTab === 0 && (
        <Box>
          <Box sx={{ mb: 2, display: 'flex', justifyContent: 'flex-end' }}>
            <Button variant="contained" color="primary" onClick={handleOpenCreateDialog}>
              Новая локация
            </Button>
          </Box>

          {loadingLocations ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
              <CircularProgress />
            </Box>
          ) : locations.length === 0 ? (
            <Paper elevation={3} sx={{ p: 3, textAlign: 'center' }}>
              <Typography variant="body1">Локации не найдены.</Typography>
            </Paper>
          ) : (
            <Grid container spacing={3}>
              {locations.map((location) => (
                <Grid item xs={12} sm={6} md={4} key={location.id}>
                  <Card><CardContent>
                      <Typography variant="h6" gutterBottom>
                        {location.name}
                      </Typography>
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                        {location.description || 'Описание отсутствует'}
                      </Typography>
                      <Button
                        variant="outlined"
                        fullWidth
                        onClick={() => handleLocationClick(location)}
                      >
                        Подробнее
                      </Button>
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          )}
        </Box>
      )}
      {activeTab === 1 && (
        <Box>
          {loadingExchanges ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
              <CircularProgress />
            </Box>
          ) : exchanges.length === 0 ? (
            <Paper elevation={3} sx={{ p: 3, textAlign: 'center' }}>
              <Typography variant="body1">Обмены не найдены.</Typography>
            </Paper>
          ) : (
            <List>
              {exchanges.map((exchange) => (
                <React.Fragment key={exchange.id}>
                  <ListItem>
                    <ListItemText
                      primary={`Обмен #${exchange.id}`}
                      secondary={`${exchange.user1.username} ↔️ ${exchange.user2.username}`}
                    />
                    <Chip
                      label={getExchangeStatus(exchange.status)}
                      color={getStatusColor(exchange.status)}
                      sx={{ mr: 2 }}
                    />
                    <Button variant="outlined" onClick={() => handleExchangeClick(exchange)}>
                      Подробнее
                    </Button>
                  </ListItem>
                  <Divider />
                </React.Fragment>
              ))}
            </List>
          )}
        </Box>
      )}
      {activeTab === 2 && (
        <Box>
          <Typography variant="h6" gutterBottom>
            Все отзывы
          </Typography>

          {loadingReviews ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
              <CircularProgress />
            </Box>
          ) : reviews.length === 0 ? (
            <Paper elevation={3} sx={{ p: 3, textAlign: 'center' }}>
              <Typography variant="body1">Отзывы не найдены.</Typography>
            </Paper>
          ) : (
            <Grid container spacing={3}>
              {reviews.map((review) => (
                <Grid item xs={12} key={review.id}>
                  <Card variant="outlined">
                    <CardContent>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                        <Typography variant="h6">{review.reviewerUsername}</Typography>
                        <Rating value={review.rating} readOnly />
                      </Box>
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                        {new Date(review.createdAt).toLocaleDateString('ru-RU', {
                          day: '2-digit',
                          month: 'long',
                          year: 'numeric'
                        })}
                      </Typography>
                      <Typography variant="body1" paragraph>
                        {review.comment || 'Без комментария'}
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          )}
        </Box>
      )}
      <Dialog
        open={detailDialogOpen && selectedLocation !== null}
        onClose={handleCloseDetailDialog}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>{selectedLocation?.name}</DialogTitle>
        <DialogContent>
          <Typography variant="body1" paragraph>
            <strong>ID:</strong> {selectedLocation?.id}
          </Typography>
          <Typography variant="body1" paragraph>
            <strong>Описание:</strong> {selectedLocation?.description || 'Описание отсутствует'}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button variant="outlined" color="error" onClick={handleOpenDeleteDialog}>
            Удалить
          </Button>
          <Button variant="outlined" onClick={handleOpenEditDialog}>
            Редактировать
          </Button>
          <Button variant="outlined" onClick={handleCloseDetailDialog}>
            Закрыть
          </Button>
        </DialogActions>
      </Dialog>
      <Dialog open={deleteDialogOpen} onClose={handleCloseDeleteDialog}>
        <DialogTitle>Подтверждение удаления локации</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Вы действительно хотите удалить локацию "{selectedLocation?.name}"? Это действие невозможно отменить.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog} variant="outlined">
            Отмена
          </Button>
          <Button onClick={handleConfirmDelete} variant="contained" color="error" autoFocus>
            Да, я действительно хочу удалить локацию
          </Button>
        </DialogActions>
      </Dialog>
      <Dialog open={editDialogOpen} onClose={handleCloseEditDialog} maxWidth="sm" fullWidth>
        <DialogTitle>Редактирование локации</DialogTitle>
        <DialogContent>
          <TextField
            label="Название"
            name="name"
            value={editLocationData.name}
            onChange={handleEditChange}
            fullWidth
            margin="normal"
            required
          />
          <TextField
            label="Описание"
            name="description"
            value={editLocationData.description}
            onChange={handleEditChange}
            fullWidth
            margin="normal"
            multiline
            rows={4}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseEditDialog} variant="outlined">
            Отмена
          </Button>
          <Button onClick={handleSaveLocation} variant="contained" color="primary">
            Сохранить
          </Button>
        </DialogActions>
      </Dialog>
      <Dialog open={createDialogOpen} onClose={handleCloseCreateDialog} maxWidth="sm" fullWidth>
        <DialogTitle>Новая локация</DialogTitle>
        <DialogContent>
          {createError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {createError}
            </Alert>
          )}
          <TextField
            label="Название"
            name="name"
            value={createLocationData.name}
            onChange={handleCreateChange}
            fullWidth
            margin="normal"
            required
          />
          <TextField
            label="Описание"
            name="description"
            value={createLocationData.description}
            onChange={handleCreateChange}
            fullWidth
            margin="normal"
            multiline
            rows={4}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseCreateDialog} variant="outlined" disabled={createLoading}>
            Отмена
          </Button>
          <Button onClick={handleCreateLocation} variant="contained" color="primary" disabled={createLoading}>
            {createLoading ? 'Создание...' : 'Создать'}
          </Button>
        </DialogActions>
      </Dialog>
      <Dialog
        open={detailDialogOpen && selectedExchange !== null}
        onClose={handleCloseDetailDialog}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>Детали обмена #{selectedExchange?.id}</DialogTitle>
        <DialogContent>
          <Box sx={{ mb: 3 }}>
            <Typography variant="h6" gutterBottom>
              Статус:
              <Chip
                label={getExchangeStatus(selectedExchange?.status)}
                color={getStatusColor(selectedExchange?.status)}
                sx={{ ml: 2 }}
              />
            </Typography>
            <Typography variant="body1" gutterBottom>
              <strong>Дата обмена:</strong>{' '}
              {selectedExchange?.exchangeDate
                ? new Date(selectedExchange.exchangeDate).toLocaleString()
                : 'Не указана'}
            </Typography>
          </Box>

          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <Typography variant="h6" gutterBottom>
                Пользователь 1: {selectedExchange?.user1?.username}
              </Typography>
              <Card variant="outlined">
                <CardContent>
                  <Typography variant="subtitle1" gutterBottom>
                    Книга: {selectedExchange?.book1?.title}
                  </Typography>
                  <Typography variant="body2">Автор: {selectedExchange?.book1?.author}</Typography>
                  <Typography variant="body2">Жанр: {selectedExchange?.book1?.genre}</Typography>
                  <Typography variant="body2">
                    Локация: {selectedExchange?.book1?.location?.name || 'Не указана'}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} md={6}>
              <Typography variant="h6" gutterBottom>
                Пользователь 2: {selectedExchange?.user2?.username}
              </Typography>
              <Card variant="outlined">
                <CardContent>
                  <Typography variant="subtitle1" gutterBottom>
                    Книга: {selectedExchange?.book2?.title}
                  </Typography>
                  <Typography variant="body2">Автор: {selectedExchange?.book2?.author}</Typography>
                  <Typography variant="body2">Жанр: {selectedExchange?.book2?.genre}</Typography>
                  <Typography variant="body2">
                    Локация: {selectedExchange?.book2?.location?.name || 'Не указана'}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button variant="outlined" onClick={handleCloseDetailDialog}>
            Закрыть
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default AdminPanelPage;