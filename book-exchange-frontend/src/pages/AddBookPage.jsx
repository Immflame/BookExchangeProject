import React, { useState, useEffect } from 'react';
import { Container, CircularProgress, Box } from '@mui/material';
import AddBookForm from '../components/AddBookForm';
import { getBookLocations } from '../api/bookService';

const AddBookPage = () => {
  const [locations, setLocations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchLocations = async () => {
      try {
        setLoading(true);
        const response = await getBookLocations();
        setLocations(response.data);
      } catch (err) {
        console.error('Ошибка загрузки локаций:', err);
        setError('Не удалось загрузить список локаций');
      } finally {
        setLoading(false);
      }
    };
    
    fetchLocations();
  }, []);

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
      {error ? (
        <Box sx={{ color: 'error.main', textAlign: 'center', mt: 3 }}>
          {error}
        </Box>
      ) : (
        <AddBookForm locations={locations} />
      )}
    </Container>
  );
};

export default AddBookPage;