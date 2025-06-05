import React from 'react';
import { Outlet } from 'react-router-dom';
import Navbar from '../components/Navbar';

const MainLayout = () => {
  return (
    <>
      <Navbar />
      <main style={{ paddingTop: '64px' }}>
        <Outlet />
      </main>
    </>
  );
};

export default MainLayout;