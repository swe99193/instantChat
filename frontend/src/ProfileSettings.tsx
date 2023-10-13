import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

// mui
import { Avatar, Box, Button, InputLabel, TextField } from "@mui/material";

// redux
import { useAppSelector } from "./redux/hooks";

// notification stack
import { useSnackbar } from 'notistack';

// components
import { fetchProfilePicture } from "./utils/fetchProfilePicture";
import { imageExtension } from "./shared/supportedFileExtension";

const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;
// const BACKEND_URL = "http://localhost:8084";  // for local testing


function ProfileSettings() {
    const navigate = useNavigate();
    const currentUserId = useAppSelector(state => state.login.userId); // Redux

    const [profilePictureUrl, setProfilePictureUrl] = useState(""); // object url

    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    // error type
    const [emptyError, setEmptyError] = useState(false);        // empty password
    const [confirmError, setConfirmError] = useState(false);    // confirm password not matched

    const { enqueueSnackbar, closeSnackbar } = useSnackbar();

    /**
     * set new password
     */
    const onSubmit = async (event) => {
        event.preventDefault();

        // check empty
        if (password.length == 0) {
            setEmptyError(true);
            return;
        } else {
            setEmptyError(false);
        }

        // check match
        if (password != confirmPassword) {
            setConfirmError(true);
            return;
        } else {
            setConfirmError(false);
        }

        const res = await fetch(`${BACKEND_URL}/user/setpassword`, {
            method: "PUT",
            body: JSON.stringify({
                password: password,
            }),
            headers: {
                'Content-type': 'application/json; charset=UTF-8',
            },
            credentials: "include"  // set cookie (session)
        });


        if (res.status == 200) {
            setPassword("");
            setConfirmPassword("");

            // success snakebar
            enqueueSnackbar("Password updated", { variant: "success", autoHideDuration: 3000, anchorOrigin: { horizontal: "center", vertical: "top" } });

        } else {
            // error snakebar
            enqueueSnackbar("Update failed", { variant: "error", autoHideDuration: 3000, anchorOrigin: { horizontal: "center", vertical: "top" } });
        }
    };


    const attachmentOnChange = async (event) => {
        const file: File = event.target.files[0];

        const fileType = file.name.split(".").pop();

        if (!imageExtension.includes(fileType)) {
            alert("🔴 Only jpeg, jpg, gif, png are supported");
            return;
        }

        event.target.value = null;  // reset file input

        if (file.size > 20000) {
            alert("🔴 File size larger than 20KB not supported");
            return;
        }

        const data = new FormData();    // multipart/form-data
        data.append("file", file);

        const snackbarId = enqueueSnackbar('Upload pending', { variant: "info", autoHideDuration: 3000 });


        const res = await fetch(`${BACKEND_URL}/user-data/profile-picture`, {
            method: "PUT",
            credentials: "include",
            body: data,
        });

        closeSnackbar(snackbarId);  // close previous message

        if (res.status != 200) {
            enqueueSnackbar("Upload failed", { variant: "error", autoHideDuration: 3000 });
            return;
        }

        enqueueSnackbar("Upload success", { variant: "success", autoHideDuration: 3000 });
        navigate(0); // refresh
    }


    const init = async () => {
        // fetch profile picture
        const objectUrl = await fetchProfilePicture(currentUserId);
        setProfilePictureUrl(objectUrl);
    }

    useEffect(() => {
        init();
    }, []);


    return (
        <Box sx={{ padding: "30px 50px" }}>
            {/* change profile picture */}
            <Avatar src={profilePictureUrl} sx={{ width: "50px", height: "50px" }} />
            <Button
                variant="contained"
                component="label"
                disableElevation
                disableRipple
                sx={{
                    background: "limegreen",
                    marginTop: "10px",
                    marginBottom: "50px",
                    ':hover': {
                        background: "limegreen"
                    },
                }}
            >
                <input
                    type="file"
                    hidden
                    onChange={attachmentOnChange}
                />
                Upload
            </Button>


            {/* change password */}
            <form onSubmit={onSubmit}>
                <InputLabel sx={{ color: "black", fontFamily: "serif" }}>New password</InputLabel>
                <TextField
                    value={password}
                    type="password"
                    onChange={(event) => setPassword(event.target.value)}
                    focused
                    error={emptyError}
                    helperText={emptyError ? "Password is empty" : ""}
                    onBlur={(e) => {
                        if (password.length == 0) {
                            setEmptyError(true);
                        } else {
                            setEmptyError(false);
                        }
                    }}
                    size="small"
                    sx={{ marginBottom: "10px", width: "100%" }}
                    inputProps={{
                        style: {
                            fontSize: "14px",
                            height: "14px",
                        }
                    }}
                />
                <InputLabel sx={{ color: "black", fontFamily: "serif" }}>Confirm new password</InputLabel>
                <TextField
                    value={confirmPassword}
                    type="password"
                    onChange={(event) => setConfirmPassword(event.target.value)}
                    focused
                    error={confirmError}
                    helperText={confirmError ? "Password do not match" : ""}
                    onBlur={(e) => {
                        if (password != confirmPassword) {
                            setConfirmError(true);
                        } else {
                            setConfirmError(false);
                        }
                    }}
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
                    Submit
                </Button>
            </form>
        </Box>
    );
}


export default ProfileSettings;