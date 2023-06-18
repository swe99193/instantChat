import React, { useRef, useState, useEffect } from 'react';
import logo from './logo.svg';
import './App.css';
import ChatLayout from './ChatLayout';
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import MainPage from './MainPage';
import Login from './Login';
import Logout from './Logout';
import Register from './Register';

function App() {
  // check login state
  const expiration = localStorage.getItem("login_expiration");
  var loggedin = true;
  if (expiration == null || Number(expiration) < Date.now()/1000){    // login expiration must exist and still valid
    loggedin = false;
  }

  return(
    <BrowserRouter>
      <Routes>
        <Route path="/" >
          <Route index element={loggedin ? <MainPage/>: <Navigate to="/login"/>}/>
          {/* <Route path='/login' Component={() => {
                window.location.replace('http://localhost:8080/login');   // backend login page
                return null;
            }}/> */}
          <Route path='/login' element={loggedin ? <Navigate to="/"/>: <Login/>}/>
          <Route path='/logout' element={<Logout/>}/>
          <Route path='/register' element={<Register/>}/>
        </Route>
        <Route path="/chat" >
          <Route index element={loggedin ? <ChatLayout/>: <Navigate to="/login"/>}/>
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
