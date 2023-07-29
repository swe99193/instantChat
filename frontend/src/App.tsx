import React, { useRef, useState, useEffect } from 'react';
import logo from './logo.svg';
import './App.css';
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import ChatLayout from './ChatLayout';
import Login from './Login';
import Logout from './Logout';
import Register from './Register';

import { useAppDispatch, useAppSelector } from './redux/hooks';
import { login } from './features/login/loginSlice';


const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;
const FRONTEND_URL = process.env.REACT_APP_FRONTEND_URL;


function App() {
    const loggedin = useAppSelector(state => state.login.loggedin); // Redux
    const dispatch = useAppDispatch(); // Redux


    const checkLoginStatus = async () => {
        console.log("🟢 App rendered")

        const res = await fetch(`${BACKEND_URL}/auth`, { credentials: "include" });
        const userId = await res.text();

        if (userId != "") {
            dispatch(login(userId)); // update Redux
        }

        console.log(userId);
    }

    useEffect(() => {
        checkLoginStatus();
    }, []);

    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={loggedin ? <ChatLayout /> : <Navigate to="/login" />} />
                {/* <Route path='/login' Component={() => {
                window.location.replace('http://localhost:8080/login');   // backend login page
                return null;
            }}/> */}
                <Route path='/login' element={loggedin ? <Navigate to="/" /> : <Login />} />
                <Route path='/logout' element={<Logout />} />
                <Route path='/register' element={<Register />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
