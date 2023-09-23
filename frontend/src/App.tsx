import { useEffect } from 'react';
// import './App.css';
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";

// redux
import { login, logout } from './features/login/loginSlice';
import { useAppDispatch, useAppSelector } from './redux/hooks';

// mui
import { GlobalStyles } from '@mui/material';
import { ThemeProvider, createTheme } from '@mui/material/styles';

// components
import { SnackbarProvider } from 'notistack';

import ChatLayout from './ChatLayout';
import Login from './Login';
import Logout from './Logout';
import Register from './Register';
import ProtectedRoute from './ProtectedRoute';
import AccountRecovery from './AccountRecovery';

// const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;
const BACKEND_URL = "http://localhost:8084";


const theme = createTheme({

});


function App() {
    const status = useAppSelector(state => state.login.status); // Redux
    const dispatch = useAppDispatch(); // Redux


    /**
     * check session status
     */
    const auth = async () => {
        console.log("🟢 App rendered")

        const res = await fetch(`${BACKEND_URL}/auth`, { credentials: "include" });
        const userId = await res.text();

        if (res.status == 200 && userId != "") {
            dispatch(login(userId)); // update Redux
        } else {
            dispatch(logout()); // update Redux
        }

        console.log(userId);
    }

    useEffect(() => {
        auth();
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
                    <Route
                        path="/"
                        element={
                            <ProtectedRoute>
                                <SnackbarProvider
                                    style={{
                                        // snackbar width
                                        minWidth: "0px",
                                        width: "180px",

                                    }}
                                >
                                    <ChatLayout />
                                </SnackbarProvider>
                            </ProtectedRoute>
                        }

                    />
                    <Route path='/login' element={<Login />} />
                    <Route path='/logout' element={<Logout />} />
                    <Route path='/join' element={<Register />} />
                    <Route path='/accountrecovery' element={<AccountRecovery />} />
                </Routes>
            </BrowserRouter>
        </ThemeProvider>
    );
}

export default App;
