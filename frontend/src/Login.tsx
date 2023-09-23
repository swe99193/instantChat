import { useState } from "react";

// redux
import { Navigate } from "react-router-dom";
import { useAppSelector } from './redux/hooks';

// mui
import { Box, Button, InputLabel, Link, Paper, Stack, TextField, Typography } from "@mui/material";

// const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;
const BACKEND_URL = "http://localhost:8084";

function Login() {
    const status = useAppSelector(state => state.login.status); // Redux

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

        const res = await fetch(`${BACKEND_URL}/login`, {
            method: "POST",
            body: JSON.stringify({
                username: username,
                password: password,
            }),
            headers: {
                'Content-type': 'application/json; charset=UTF-8',
            },
            credentials: "include"  // set cookie (session)

        });

        // login success
        if (res.status == 200) {
            setError(false);
            setSuccess(true);

            await new Promise(r => setTimeout(r, 2000));
            window.location.replace("");  // redirect to main page
        } else {
            setError(true);
            setErrorMessage("invalid username or password 😢");
        }
    };


    if (status == "init")
        return (<></>);     // blank page during authentication (better user experience)
    else if (status == "login")
        return (<Navigate to="/" />);
    else    // status == "logout"
        return (
            <Box sx={{ display: "flex", alignItems: "center", justifyContent: "center", height: "100vh" }}>
                <Paper elevation={8} sx={{ width: "220px", padding: "30px" }}>
                    <Typography sx={{ fontSize: "24px", marginBottom: "20px", fontFamily: "serif" }}>
                        Sign In
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
                                        Login
                                    </Button>
                                </div>
                            }

                            {
                                success &&
                                <Typography sx={{ fontSize: "14px", color: "black", marginBottom: "10px", fontFamily: "serif" }}>
                                    {`Welcome back, ${username} 😇`}
                                </Typography>
                            }
                        </form>

                        {/* forget password */}
                        <Link href="/accountrecovery" underline="none" sx={{ fontSize: "14px", textAlign: "center", marginTop: "20px" }} > forgot password? </Link>
                        {/* register */}
                        <Link href="/join" underline="none" sx={{ fontSize: "14px", textAlign: "center", marginTop: "10px" }}> create account </Link>

                    </Stack>
                </Paper>
            </Box>
        );
}

export default Login;