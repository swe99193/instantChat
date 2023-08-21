import React from "react";

// import "./Register.css";

const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;

function Register() {
    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();

        // post form by fetch api
        let formData = new FormData();
        formData.append('username', (document.getElementById("username") as HTMLInputElement).value);
        formData.append('password', (document.getElementById("password") as HTMLInputElement).value);

        // const res = await fetch(`${BACKEND_URL}/register`, {
        const res = await fetch(`http://localhost:8084/register`, {
            method: "POST",
            body: formData
        });

        alert("Register an account successful");
        window.location.replace(`/login`); // redirect

    }

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
                <div className="button-container">
                    <input type="submit" value="Register " />
                </div>
            </form>
        </div>
    );

    return (
        <div className="app">
            <div className="register-form">
                <div className="title">Register Now</div>
                {renderForm}
            </div>
        </div>
    );
}

export default Register;