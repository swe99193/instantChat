import React, { useRef, useState, useEffect } from 'react';
import logo from './logo.svg';
import './App.css';
import ChatLayout from './ChatLayout';
import { BrowserRouter, Routes, Route } from "react-router-dom";
import MainPage from './MainPage';

function App() {

  return(
    <BrowserRouter>
      <Routes>
        <Route path="/" >
          <Route index element={<MainPage/>}/>
        </Route>
        <Route path="/chat" >
          <Route index element={<ChatLayout/>}/>
          <Route path="*"/>
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
