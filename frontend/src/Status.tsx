import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

// redux
import { useAppSelector } from "./redux/hooks";

// mui
import { Box, Button, TextField, Typography } from "@mui/material";

// notification stack
import { useSnackbar } from "notistack";


// const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;
const BACKEND_URL = "http://localhost:8084";


function Status() {
    const navigate = useNavigate();
    const currentUserId = useAppSelector(state => state.login.userId); // Redux

    const [statusMessage, setStatusMessage] = useState("");

    const { enqueueSnackbar, closeSnackbar } = useSnackbar();

    /**
     * set new status message
     */
    const onSubmit = async (event) => {
        event.preventDefault();

        const res = await fetch(`${BACKEND_URL}/user-data/status-message`, {
            method: "PUT",
            body: JSON.stringify({
                statusMessage: statusMessage,
            }),
            headers: {
                'Content-type': 'application/json; charset=UTF-8',
            },
            credentials: "include"  // set cookie (session)
        });


        if (res.status == 200) {
            navigate(0); // refresh

        } else {
            // error snakebar
            enqueueSnackbar('Update failed', { variant: "error", autoHideDuration: 3000, anchorOrigin: { horizontal: "center", vertical: "top" } });
        }
    };


    const init = async () => {
        // fetch statusMessage
        const params = new URLSearchParams({
            username: currentUserId,
        });
        const res = await fetch(`${BACKEND_URL}/user-data?${params}`, { credentials: "include" });

        setStatusMessage((await res.json()).statusMessage);
    }

    useEffect(() => {
        init();
    }, []);


    return (
        <Box sx={{ padding: "30px 50px" }}>

            <Typography
                sx={{
                    fontFamily: "serif",
                    fontSize: "20px",
                    marginBottom: "10px",
                }}
            >
                Set your status
            </Typography>

            <form onSubmit={onSubmit}>
                <TextField
                    value={statusMessage}
                    type="text"
                    placeholder="What's on your mind?"
                    onChange={(event) => setStatusMessage(event.target.value)}
                    focused
                    size="small"
                    sx={{ marginBottom: "10px", width: "100%" }}
                    inputProps={{
                        style: {
                            fontSize: "14px",
                            height: "14px",
                        }
                    }}
                />

                <Button
                    variant="contained"
                    type="submit"
                    disableElevation
                    disableRipple
                    sx={{
                        background: "limegreen",
                        marginTop: "10px",
                        ':hover': {
                            background: "limegreen"
                        },
                    }}
                >
                    Update
                </Button>
            </form>
        </Box>
    );
}


export default Status;