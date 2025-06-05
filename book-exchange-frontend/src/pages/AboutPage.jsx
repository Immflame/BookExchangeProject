import React from 'react';
import { Container, Typography, Box } from '@mui/material';

const AboutPage = () => {
  return (
    <Container maxWidth="md">
      <Box sx={{ mt: 4 }}>
        <Typography variant="h4" gutterBottom>
          О нашем сервисе
        </Typography>
        
        <Typography variant="body1" paragraph>
          Добро пожаловать на платформу для обмена книгами "BookExchange"! Наш сервис создан для любителей чтения, 
          которые хотят делиться книгами с другими читателями.
        </Typography>
        
        <Typography variant="h5" gutterBottom sx={{ mt: 3 }}>
          Как это работает?
        </Typography>
        <Typography variant="body1" paragraph>
          1. Добавьте книги, которыми хотите поделиться
        </Typography>
        <Typography variant="body1" paragraph>
          2. Положите их на полку в любой удобной для вас локации
        </Typography>
        <Typography variant="body1" paragraph>
          3. Найдите интересующие вас книги в каталоге
        </Typography>
        <Typography variant="body1" paragraph>
          4. Предложите обмен владельцу книги
        </Typography>
        <Typography variant="body1" paragraph>
          5. Заберите интересующую вас книгу и подтвердите обмен ! 
        </Typography>
        
        <Typography variant="h5" gutterBottom sx={{ mt: 3 }}>
          Наши преимущества
        </Typography>
        
        <Typography variant="body1" paragraph>
          ✓ Бесплатный обмен книгами
        </Typography>
        <Typography variant="body1" paragraph>
          ✓ Возможность найти любую книгу по душе
        </Typography>
        <Typography variant="body1" paragraph>
          ✓ Сообщество единомышленников
        </Typography>
        <Typography variant="body1" paragraph>
          ✓ Удобные точки обмена
        </Typography>
        
        <Typography variant="h5" gutterBottom sx={{ mt: 3 }}>
          Наша миссия
        </Typography>
        
        <Typography variant="body1" paragraph>
          Мы стремимся создать сообщество читателей, где книги обретают новую жизнь, 
          а люди находят единомышленников. Наша цель - сделать чтение доступным и 
          приятным для всех, развивать культуры книгообмена.
        </Typography>
        
        <Typography variant="h5" gutterBottom sx={{ mt: 3 }}>
          Контакты
        </Typography>
        <Typography variant="body1" paragraph>
          Email: The.scorpion2006@yandex.ru
        </Typography>
        <Typography variant="body1" paragraph>
          Телефон: +7 (937) 353-76-45
        </Typography>
      </Box>
    </Container>
  );
};

export default AboutPage;