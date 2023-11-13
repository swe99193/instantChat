import { useEffect, useRef, useState } from 'react';
import Stomp from 'stompjs';

// redux
import { useAppSelector } from './redux/hooks';

// mui
import { Avatar, Badge, Box, IconButton, InputAdornment, List, ListItemAvatar, ListItemButton, ListItemText, TextField, Typography } from '@mui/material';

// mui icons
import { Menu, Search } from '@mui/icons-material';

// components
import ChatRoom from './ChatRoom';
import NavigationDrawer from './NavigationDrawer';

// utils
import { fetchProfilePicture } from './utils/fetchProfilePicture';


const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;
// const BACKEND_URL_1 = "http://localhost:8082";  // for local testing
// const BACKEND_URL_2 = "http://localhost:8084";  // for local testing
const WEBSOCKET_ENDPOINT = process.env.REACT_APP_WEBSOCKET_ENDPOINT;


interface Conversation {
    username: string,
    profilePictureUrl: string
    latestMessage: string;
}


function ChatLayout() {

    const [conversationList, setConversationList] = useState<Conversation[]>([]);
    const usernameSet = useRef(new Set()); // a set of username in conversation list
    const [receiver, setReceiver] = useState("");   // receiver of active conversation
    const [profilePictureUrl, setProfilePictureUrl] = useState(""); // object url of active conversation, passed to children components

    const stompClient = useRef<Stomp.Client>(Stomp.client(`${WEBSOCKET_ENDPOINT}`));
    const [isConnected, setIsConnected] = useState(false); // whether the Stomp client has established connection

    const currentUserId = useAppSelector(state => state.login.userId); // Redux

    // drawer
    const [open, setOpen] = useState(false);

    /**
     * render list of conversation users in side bar
     */
    const fetchConversation = async () => {

        const res = await fetch(`${BACKEND_URL}/conversation`, { credentials: "include" });

        if (res.status != 200) {
            alert("🔴 Server error");
            return;
        }

        const list: Array<any> = await res.json();

        const arr = list.map(async (item) => {
            const username = item.user1 == currentUserId ? item.user2 : item.user1;
            usernameSet.current.add(username);

            // fetch profile picture
            const objectUrl = await fetchProfilePicture(username);

            return {
                username: username,
                profilePictureUrl: objectUrl,
                latestMessage: item.latestMessage
            } as Conversation;
        });

        setConversationList(await Promise.all(arr));
    }

    const ConversationOnClick = (username: string) => (event) => {
        // wait for stomp connection
        if (!isConnected)
            return;

        setReceiver(username);
        setProfilePictureUrl(conversationList.filter(item => item.username == username)[0].profilePictureUrl);
    }

    const startNewChat = async (event: any) => {
        const username = event.target.value;

        if (event.key != "Enter" || !username)  // not Enter key or empty input
            return

        // wait for stomp connection
        if (!isConnected)
            return;


        if (!/^[a-zA-Z0-9]+$/.test(username)) {  // check alphebet and numeric
            alert("🔴 Error: invalid username");
            return;
        }

        // chech user exists
        const params = new URLSearchParams({
            username: username
        });

        const res = await fetch(`${BACKEND_URL}/user/exists?${params}`, { credentials: "include" });

        const resJson = await res.json();

        if (res.status != 200) {
            alert("🔴 Server error");
            return;
        }
        else if (resJson == false) {
            alert("🔴 Error: user does not exist");
            return;
        }

        setReceiver(username);

        // check if user in the list, and update conversation list
        if (!usernameSet.current.has(username)) {

            // fetch profile picture
            const objectUrl = await fetchProfilePicture(username);

            // insert new entry
            const newEntry: Conversation = {
                username: username,
                profilePictureUrl: objectUrl,
                latestMessage: ""
            }

            setProfilePictureUrl(objectUrl);

            setConversationList(conversations => {
                return [newEntry].concat(conversations);
            });

            // update username set
            usernameSet.current.add(username);
        }
        else {
            setProfilePictureUrl(conversationList.filter(item => item.username == username)[0].profilePictureUrl);
        }
    }

    useEffect(() => {
        fetchConversation();

        stompClient.current.connect({}, function (frame) {
            console.log('Connected: ' + frame);
            setIsConnected(true);
        });
    }, []);


    return (
        <div style={{ "height": "100vh", display: "flex" }}>

            {/* side bar (navigation) */}
            <NavigationDrawer open={open} setOpen={setOpen} />

            {/* side bar (conversation) */}
            <Box sx={{
                minWidth: "300px",
                maxWidth: "300px",
                overflow: "auto",
                borderRight: "1px solid lightgrey",
            }}>

                <div style={{ display: "flex", alignItems: "center" }}>
                    <IconButton onClick={() => setOpen(true)}>
                        <Menu />
                    </IconButton>

                    <TextField
                        variant="outlined"
                        autoFocus={false}
                        label=""
                        placeholder="Search username"
                        onKeyDown={startNewChat}
                        autoComplete="off"
                        sx={{ "margin": "10px", borderColor: "red" }}
                        size="small"
                        InputProps={{
                            startAdornment: (
                                // search icon
                                <InputAdornment position="start">
                                    <Search />
                                </InputAdornment>
                            )
                        }}
                    />
                </div>

                {/* Conversation list */}
                <List>
                    {
                        conversationList.map((item, idx) =>
                            <ListItemButton alignItems="flex-start" selected={receiver == item.username} onClick={ConversationOnClick(item.username)} sx={{ borderRadius: "5px", margin: "3px 5px" }}>
                                <ListItemAvatar>
                                    {/* TODO: online status */}
                                    {/* <Badge variant="dot" anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }} color="success" invisible={false} sx={{
                                        '& .MuiBadge-badge': {
                                            backgroundColor: '#32cd32',
                                            color: '#32cd32',
                                            boxShadow: `0 0 0 2px white`,
                                        },
                                    }} overlap="circular"> */}
                                    <Avatar src={item.profilePictureUrl} />
                                    {/* </Badge> */}
                                </ListItemAvatar>
                                <ListItemText
                                    primary={
                                        <Typography
                                            sx={{ display: 'inline' }}
                                            component="span"
                                            variant="body2"
                                            color="text.primary"
                                        >
                                            {item.username}
                                        </Typography>}

                                    secondary={item.latestMessage}
                                />
                                {/* TODO: unread message counts */}
                                {/* adjust Badge position: https://stackoverflow.com/questions/71399377/how-to-position-mui-badge-in-iconbutton-border-in-reactjs */}
                                <Badge badgeContent={4} color="error" invisible={false} style={{ transform: 'translate(0px, 25px)' }}></Badge>
                            </ListItemButton>
                        )
                    }
                </List>
            </Box>

            {/* chat contrainer */}
            {receiver ? <ChatRoom stompClient={stompClient.current} receiver={receiver} profilePictureUrl={profilePictureUrl} /> : <></>}
        </div>
    );
}

export default ChatLayout;
