const genreMap = {
  FANTASY: 'Фэнтези',
  SCIENCE_FICTION: 'Научная фантастика',
  DETECTIVE: 'Детектив',
  BIOGRAPHY: 'Биография/Автобиография',
  HISTORY: 'История',
  SCIENCE_TECHNOLOGY: 'Наука и технологии',
  SELF_IMPROVEMENT: 'Саморазвитие'
};

export const getGenreDisplayName = (genre) => {
  return genreMap[genre] || genre;
};

export const genreOptions = Object.entries(genreMap).map(([value, label]) => ({
  value,
  label
}));