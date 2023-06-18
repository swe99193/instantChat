import React, { useState } from "react";
import ReactDOM from "react-dom";

function Logout() {
    localStorage.removeItem("login_expiration");

    window.location.replace('http://localhost:8080/logout'); // call logout at backend
    window.location.replace('http://localhost:3000/login'); // redirect to login page
    alert("You've logged out");

    return null;
}

export default Logout;