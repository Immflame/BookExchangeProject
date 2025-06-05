import React, { useState } from 'react';
import { 
  Box, 
  TextField, 
  FormControl, 
  InputLabel, 
  Select, 
  MenuItem, 
  Button,
  CircularProgress,
  Typography
} from '@mui/material';
import { genreOptions } from '../utils/genreHelper';
import { useNavigate } from 'react-router-dom';
import { createBook } from '../api/bookService';

const AddBookForm = ({ locations }) => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [formData, setFormData] = useState({
    title: '',
    author: '',
    description: '',
    genre: '',
    locationId: ''
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    
    try {
      const token = localStorage.getItem('token');
      const bookData = {
        token,
        title: formData.title,
        author: formData.author,
        description: formData.description,
        genre: formData.genre,
        locationId: formData.locationId
      };
      
      const response = await createBook(bookData);
      
      if (response.data && response.data.id) {
        navigate(`/book/${response.data.id}`);
      } else {
        setError('Не удалось создать книгу');
      }
    } catch (err) {
      console.error('Ошибка создания книги:', err);
      setError(err.response?.data?.message || 'Ошибка сервера');
    } finally {
      setLoading(false);
    }
  };

  const isFormValid = () => {
    return (
      formData.title.trim() !== '' &&
      formData.author.trim() !== '' &&
      formData.genre !== '' &&
      formData.locationId !== ''
    );
  };

  return (
    <Box component="form" onSubmit={handleSubmit} sx={{ maxWidth: 600, mx: 'auto' }}>
      <Typography variant="h5" gutterBottom>
        Добавить новую книгу
      </Typography>
      
      {error && (
        <Typography color="error" sx={{ mb: 2 }}>
          {error}
        </Typography>
      )}
      
      <TextField
        label="Название книги"
        name="title"
        value={formData.title}
        onChange={handleChange}
        fullWidth
        margin="normal"
        required
      />
      
      <TextField
        label="Автор"
        name="author"
        value={formData.author}
        onChange={handleChange}
        fullWidth
        margin="normal"
        required
      />
      
      <TextField
        label="Описание"
        name="description"
        value={formData.description}
        onChange={handleChange}
        fullWidth
        margin="normal"
        multiline
        rows={4}
      />
      
      <FormControl fullWidth margin="normal" required>
        <InputLabel>Жанр</InputLabel>
        <Select
          name="genre"
          value={formData.genre}
          onChange={handleChange}
          label="Жанр"
        >
          <MenuItem value="" disabled>Выберите жанр</MenuItem>
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
          value={formData.locationId}
          onChange={handleChange}
          label="Локация обмена"
        >
          <MenuItem value="" disabled>Выберите локацию</MenuItem>
          {locations.map((location) => (
            <MenuItem key={location.id} value={location.id}>
              {location.name}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
      
      <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
        <Button
          variant="outlined"
          onClick={() => navigate('/')}
        >
          Отмена
        </Button>
        
        <Button
          type="submit"
          variant="contained"
          color="primary"
          disabled={!isFormValid() || loading}
        >
          {loading ? <CircularProgress size={24} /> : 'Добавить книгу'}
        </Button>
      </Box>
    </Box>
  );
};

export default AddBookForm;