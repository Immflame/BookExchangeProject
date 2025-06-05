import React, { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import AuthPage from './pages/AuthPage';
import HomePage from './pages/HomePage';
import AboutPage from './pages/AboutPage';
import ProfilePage from './pages/ProfilePage';
import AddBookPage from './pages/AddBookPage';
import MyExchangesPage from './pages/MyExchangesPage';
import AdminPanelPage from './pages/AdminPanelPage';
import UserProfilePage from './pages/UserProfilePage';
import { isAuthenticated } from './utils/auth';
import MainLayout from './layouts/MainLayout';

const App = () => {
  const [authenticated, setAuthenticated] = useState(isAuthenticated());

  useEffect(() => {
    const handleAuthChange = () => {
      setAuthenticated(isAuthenticated());
    };

    window.addEventListener('authChange', handleAuthChange);
    return () => window.removeEventListener('authChange', handleAuthChange);
  }, []);

  return (
    <BrowserRouter>
      <Routes>
        <Route 
          path="/auth" 
          element={!authenticated ? <AuthPage /> : <Navigate to="/" />} 
        />
        <Route element={<MainLayout />}>
          <Route 
            path="/" 
            element={authenticated ? <HomePage /> : <Navigate to="/auth" />} 
          />
          <Route 
            path="/about" 
            element={authenticated ? <AboutPage /> : <Navigate to="/auth" />} 
          />
          <Route 
            path="/profile" 
            element={authenticated ? <ProfilePage /> : <Navigate to="/auth" />} 
          />
          <Route 
            path="/add-book" 
            element={authenticated ? <AddBookPage /> : <Navigate to="/auth" />} 
          />
          <Route 
            path="/my-exchanges" 
            element={authenticated ? <MyExchangesPage /> : <Navigate to="/auth" />} 
          />
          <Route 
            path="/admin" 
            element={authenticated ? <AdminPanelPage /> : <Navigate to="/auth" />} 
          />
          <Route 
            path="/user/:id" 
            element={authenticated ? <UserProfilePage /> : <Navigate to="/auth" />} 
          />
        </Route>
        
        <Route path="*" element={<Navigate to={authenticated ? "/" : "/auth"} />} />
      </Routes>
    </BrowserRouter>
  );
};

export default App;