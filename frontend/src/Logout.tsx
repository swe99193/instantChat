import React, { useState } from "react";
import ReactDOM from "react-dom";

const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;
const FRONTEND_URL = process.env.REACT_APP_FRONTEND_URL;

function Logout() {
    localStorage.removeItem("login_expiration");

    window.location.replace(`${BACKEND_URL}/logout`); // call logout at backend
    window.location.replace(`${FRONTEND_URL}/login`); // redirect to login page
    alert("You've logged out");

    return null;
}

export default Logout;