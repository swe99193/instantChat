import { useEffect } from 'react';
// import './App.css';
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";

// redux
import { login } from './features/login/loginSlice';
import { useAppDispatch, useAppSelector } from './redux/hooks';

// mui
import { GlobalStyles } from '@mui/material';
import { ThemeProvider, createTheme } from '@mui/material/styles';

// components
import ChatLayout from './ChatLayout';
import Login from './Login';
import Logout from './Logout';
import Register from './Register';

const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;


const theme = createTheme({

});


function App() {
    const loggedin = useAppSelector(state => state.login.loggedin); // Redux
    const dispatch = useAppDispatch(); // Redux


    const checkLoginStatus = async () => {
        console.log("🟢 App rendered")

        // const res = await fetch(`${BACKEND_URL}/auth`, { credentials: "include" });
        const res = await fetch(`http://localhost:8084/auth`, { credentials: "include" });
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
        <ThemeProvider theme={theme}>

            {/* global css*/}
            <GlobalStyles styles={{
                // scrollbar
                '*::-webkit-scrollbar': {
                    width: '8px'
                },
                '*::-webkit-scrollbar-track': {

                },
                '*::-webkit-scrollbar-thumb': {
                    backgroundColor: '#DCDCDC',
                    borderRadius: "20px"
                },
                height: "100vh",
                overflow: "scroll"
            }} />

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
        </ThemeProvider>
    );
}

export default App;
