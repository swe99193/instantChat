import React, { useState } from "react";
import ReactDOM from "react-dom";
import { useAppDispatch } from "./redux/hooks";
import { logout } from "./features/login/loginSlice";

const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;
const FRONTEND_URL = process.env.REACT_APP_FRONTEND_URL;

function Logout() {
    const dispatch = useAppDispatch(); // redux

    window.location.replace(`${BACKEND_URL}/logout`); // call logout at backend
    window.location.replace(`${FRONTEND_URL}/login`); // redirect to login page

    dispatch(logout()); // update Redux

    alert("You've logged out");

    return null;
}

export default Logout;