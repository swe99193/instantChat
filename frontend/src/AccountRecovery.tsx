import React, { useState } from "react";

import { Navigate } from "react-router-dom";

// mui
import { Box, Button, InputLabel, Link, Paper, Stack, TextField, Typography } from "@mui/material";

// const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;
const BACKEND_URL = "http://localhost:8084";

function AccountRecovery() {
    const [success, setSuccess] = useState(false);
    const [error, setError] = useState(false);
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [errorMessage, setErrorMessage] = useState("");


    const onSubmit = async (event) => {
        event.preventDefault();

        if (username.length == 0 || password.length == 0) {
            setError(true);
            setErrorMessage("username or password empty");
            return;
        } else if (!/^[A-Za-z0-9]+$/.test(username)) {
            setError(true);
            setErrorMessage("username must be alphanumeric");
            return;
        }

        // post form by fetch api
        let formData = new FormData();
        formData.append('username', username);
        formData.append('password', password);

        const res = await fetch(`${BACKEND_URL}/accountrecovery`, {
            method: "PUT",
            body: formData,
            credentials: "include"
        });

        // login success
        if (res.status == 200) {
            setError(false);
            setSuccess(true);

            await new Promise(r => setTimeout(r, 2000));
            window.location.replace("/login");
        } else if (res.status == 404) {
            setError(true);
            setErrorMessage("Username does not exist 😢");
        } else {
            setError(true);
            setErrorMessage("Something went wrong 😢");
        }
    };

    return (
        <Box sx={{ display: "flex", alignItems: "center", justifyContent: "center", height: "100vh" }}>
            <Paper elevation={8} sx={{ width: "220px", padding: "30px" }}>
                <Typography sx={{ fontSize: "24px", marginBottom: "20px", fontFamily: "serif" }}>
                    Set new password
                </Typography>

                <Stack margin="0px 6px">
                    <form onSubmit={onSubmit}>
                        <InputLabel sx={{ color: "black", fontFamily: "serif" }}>Username</InputLabel>
                        <TextField
                            value={username}
                            required
                            type="text"
                            onChange={(event) => setUsername(event.target.value)}
                            size="small"
                            sx={{ marginBottom: "10px", width: "100%" }}
                            inputProps={{
                                style: {
                                    fontSize: "14px",
                                    height: "14px",
                                }
                            }}
                        />

                        <InputLabel sx={{ color: "black", fontFamily: "serif" }}>Password</InputLabel>
                        <TextField
                            value={password}
                            type="password"
                            onChange={(event) => setPassword(event.target.value)}
                            size="small"
                            sx={{ marginBottom: "10px", width: "100%" }}
                            inputProps={{
                                style: {
                                    fontSize: "14px",
                                    height: "14px",
                                }
                            }}
                        />

                        {
                            error &&
                            <Typography sx={{ fontSize: "14px", color: "red", fontFamily: "serif" }}>
                                {errorMessage}
                            </Typography>
                        }

                        {
                            !success &&
                            <div style={{ textAlign: "center" }}>
                                <Button variant="contained" type="submit" disableRipple disableElevation sx={{ width: "100px", background: "DodgerBlue", marginTop: "10px" }} onClick={onSubmit}>
                                    Submit
                                </Button>
                            </div>
                        }

                        {
                            success &&
                            <Typography sx={{ fontSize: "14px", color: "black", marginBottom: "10px", fontFamily: "serif" }}>
                                {`Password reset 😇`}
                            </Typography>
                        }
                    </form>

                    {/* login */}
                    <Link href="/login" underline="none" sx={{ fontSize: "14px", textAlign: "center", marginTop: "20px" }}> Already have account? </Link>

                </Stack>
            </Paper>
        </Box>
    );
}

export default AccountRecovery;