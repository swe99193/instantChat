import React, { useEffect, useState } from "react";
import ReactDOM from "react-dom";

import "./Login.css";

const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;
const FRONTEND_URL = process.env.REACT_APP_FRONTEND_URL;

function Login() {
  const [errorMessages, setErrorMessages] = useState("");
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [username, setUsername] = useState("");

  // post form by fetch api
  // ref: https://stackoverflow.com/questions/46640024/how-do-i-post-form-data-with-fetch-api
  const handleSubmit = async (event: React.FormEvent) => {

    event.preventDefault();

    let formData = new FormData();
    formData.append('username', (document.getElementById("username") as HTMLInputElement).value);
    formData.append('password', (document.getElementById("password") as HTMLInputElement).value);

    const res = await fetch(`${BACKEND_URL}/login`, {
        method: "POST", 
        body: formData,
        credentials: "include"  // set cookie (session)
    });

    if (res.status == 200) {
      // login success
      setUsername((document.getElementById("username") as HTMLInputElement).value)
      setIsSubmitted(true);

      await new Promise(r => setTimeout(r, 1000));
      window.location.replace(`${FRONTEND_URL}`);  // redirect to main page

    } else {
      // Invalid username & password, or other errors
      setErrorMessages("invalid username or password");
    }
  };

  const renderForm = (
    <div className="form">
      <form onSubmit={handleSubmit}>
        <div className="input-container">
          <label>Username </label>
          <input type="text" id="username" name="username" required />
        </div>
        <div className="input-container">
          <label>Password </label>
          <input type="password" id="password" name="password" required />
        </div>
        <div className="error"> {errorMessages} </div>
        <div className="button-container">
          <input type="submit" />
        </div>
      </form>
    </div>
  );

  return (
    <div className="app">
      <div className="login-form">
        <div className="title">Sign In</div>
        {isSubmitted ? <div> Welcome Back, {username} </div> : renderForm}
      </div>
    </div>
  );
}

export default Login;