import { useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";

// redux
import { useAppSelector } from "./redux/hooks";

// mui
import { Mood, AccountBox, Logout } from "@mui/icons-material"
import { Avatar, Dialog, Divider, Drawer, List, ListItem, ListItemAvatar, ListItemButton, ListItemIcon, ListItemText } from "@mui/material"

// components
import Status from "./Status";
import ProfileSettings from "./ProfileSettings";

// utils
import { fetchProfilePicture } from "./utils/fetchProfilePicture";


// const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;
const BACKEND_URL = "http://localhost:8084";


/**
 * side bar (navigation)
 */
function NavigationDrawer({ open, setOpen }) {
    const navigate = useNavigate();
    const currentUserId = useAppSelector(state => state.login.userId); // Redux

    const [profilePictureUrl, setProfilePictureUrl] = useState(""); // profile picture

    const [openStatus, setOpenStatus] = useState(false);
    const [openProfile, setOpenProfile] = useState(false);

    const init = async () => {
        // fetch profile picture
        const objectUrl = await fetchProfilePicture(currentUserId);
        setProfilePictureUrl(objectUrl);
    }

    useEffect(() => {
        init();
    }, []);


    return (
        <>
            <Drawer
                sx={{
                    width: "300px",
                    flexShrink: 0,
                    '& .MuiDrawer-paper': {
                        width: "300px",
                        boxSizing: 'border-box',
                    },
                }}
                variant="temporary"
                anchor="left"
                open={open}
                onClose={() => setOpen(false)}
            >

                {/* profile setting, set status, logout */}
                <List>
                    <ListItem disablePadding >
                        <ListItemAvatar sx={{ padding: "10px" }}>
                            <Avatar src={profilePictureUrl} />
                        </ListItemAvatar>

                        <ListItemText>{currentUserId}</ListItemText>
                    </ListItem>

                    <Divider />

                    <ListItem key={"status"} disablePadding onClick={() => setOpenStatus(true)}>
                        <ListItemButton>
                            <ListItemIcon>
                                <Mood />
                            </ListItemIcon>
                            <ListItemText primary={"Set status"} />
                        </ListItemButton>
                    </ListItem>

                    <ListItem key={"profile"} disablePadding onClick={() => setOpenProfile(true)}>
                        <ListItemButton>
                            <ListItemIcon>
                                <AccountBox />
                            </ListItemIcon>
                            <ListItemText primary={"Your profile"} />
                        </ListItemButton>
                    </ListItem>

                    <ListItem key={"logout"} disablePadding>
                        <ListItemButton>
                            <ListItemIcon>
                                <Logout />
                            </ListItemIcon>
                            <ListItemText primary={"Sign out"} onClick={() => navigate("/logout")} />
                        </ListItemButton>
                    </ListItem>
                </List>
            </Drawer>

            <Dialog maxWidth="sm" fullWidth PaperProps={{ sx: { borderRadius: "10px" } }} open={openStatus} onClose={() => setOpenStatus(false)}>
                <Status />
            </Dialog>

            <Dialog maxWidth="sm" fullWidth PaperProps={{ sx: { borderRadius: "10px" } }} open={openProfile} onClose={() => setOpenProfile(false)}>
                <ProfileSettings />
            </Dialog>
        </>
    );
}


export default NavigationDrawer;