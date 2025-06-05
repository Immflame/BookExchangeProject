import React from 'react';
import { 
  Box, 
  Typography, 
  FormControl, 
  InputLabel, 
  Select, 
  MenuItem, 
  Chip, 
  Button,
  Grid
} from '@mui/material';
import { genreOptions, getGenreDisplayName } from '../utils/genreHelper';

const BookFilters = ({ 
  locations, 
  onApplyFilters,
  selectedGenres = [],
  selectedLocationIds = []
}) => {
  const [tempGenre, setTempGenre] = React.useState(selectedGenres[0] || '');
  const [tempLocation, setTempLocation] = React.useState(selectedLocationIds[0] || 0);

  const handleGenreChange = (e) => {
    setTempGenre(e.target.value);
  };

  const handleLocationChange = (e) => {
    setTempLocation(e.target.value);
  };

  const applyFilters = () => {
    onApplyFilters({
      genres: tempGenre ? [tempGenre] : [],
      locationIds: tempLocation ? [tempLocation] : []
    });
  };

  const clearFilters = () => {
    setTempGenre('');
    setTempLocation(0);
    onApplyFilters({
      genres: [],
      locationIds: []
    });
  };

  return (
    <Box sx={{ mb: 4, p: 3, border: '1px solid #e0e0e0', borderRadius: 1 }}>
      <Typography variant="h6" gutterBottom>
        Фильтры
      </Typography>
      
      <Grid container spacing={2} wrap="nowrap" alignItems="center">
        <Grid item xs={12} md={4}>
          <FormControl fullWidth sx={{ width: 180 }}>
            <InputLabel>Жанр</InputLabel>
            <Select
              value={tempGenre}
              onChange={handleGenreChange}
              label="Жанр"
            >
              <MenuItem value={0}>Все жанры</MenuItem>
              {genreOptions.map((genre) => (
                <MenuItem key={genre.value} value={genre.value}>
                  {genre.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Grid>
        
        <Grid item xs={12} md={4}>
          <FormControl fullWidth sx={{ width: 180 }}>
            <InputLabel>Локация</InputLabel>
            <Select
              value={tempLocation}
              onChange={handleLocationChange}
              label="Локация"
            >
              <MenuItem value={0}>Все локации</MenuItem>
              {locations.map((location) => (
                <MenuItem key={location.id} value={location.id}>
                  {location.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Grid>
        <Grid item xs={12} md={4} sx={{ display: 'flex', gap: 1 }}>
          <Button 
            variant="contained" 
            color="primary"
            onClick={applyFilters}
            fullWidth
          >
            Применить
          </Button>
          <Button 
            variant="outlined" 
            color="secondary"
            onClick={clearFilters}
            fullWidth
          >
            Сбросить
          </Button>
        </Grid>
      </Grid>
    </Box>
  );
};

export default BookFilters;