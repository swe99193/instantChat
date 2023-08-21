import { useEffect, useRef, useState } from 'react';
import Stomp from 'stompjs';

// mui
import { Avatar, Badge, Drawer, InputAdornment, List, ListItemAvatar, ListItemButton, ListItemText, TextField, Typography } from '@mui/material';

// mui icons
import { Search } from '@mui/icons-material';

// components
import ChatRoom from './ChatRoom';


const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;
const WEBSOCKET_ENDPOINT = process.env.REACT_APP_WEBSOCKET_ENDPOINT;

interface userObject {
    username: string,
    chatUser: string
}

interface conversation {
    username: string,
    profilePictureUrl: string
}

function ChatLayout() {
    const [conversationList, setConversationList] = useState<conversation[]>([]);
    const userMap = useRef<any>({}); // { username -> true }
    const [receiver, setReceiver] = useState("");

    const stompClient = useRef<Stomp.Client>(Stomp.client(`ws://${WEBSOCKET_ENDPOINT}/gs-guide-websocket`));
    const [isConnected, setIsConnected] = useState(false); // whether the Stomp client has established connection


    /**
     * render list of conversation users in side bar
     */
    const fetchConversation = async () => {

        // const response = await fetch(`${BACKEND_URL}/conversation`, { credentials: "include" });
        const response = await fetch(`http://localhost:8082/conversation`, { credentials: "include" });

        let list: Array<any> = await response.json();

        setConversationList(conversation => {
            let arr = list.map((userObject: userObject) => {
                const username = userObject["chatUser"]
                // initialize 
                userMap.current[username] = true;

                return {
                    username: username,
                    profilePictureUrl: "https://external-preview.redd.it/1mF2BkbuRUyI5Od8V7aTZDVS_Y8-GMWeT4zvv7e_IrI.jpg?auto=webp&s=6dd561c5c1c1d69de69a56c8afaf4d5e3269d537",
                };
            });
            return conversation.concat(arr);
        });
    }

    const ConversationOnClick = (username: string) => (event) => {
        // wait for stomp connection
        if (!isConnected)
            return;

        setReceiver(username);

        // marked the active conversation
        setConversationList(conversation => {
            return conversation.map((item: conversation) => {
                return {
                    username: item["username"],
                    profilePictureUrl: "https://external-preview.redd.it/1mF2BkbuRUyI5Od8V7aTZDVS_Y8-GMWeT4zvv7e_IrI.jpg?auto=webp&s=6dd561c5c1c1d69de69a56c8afaf4d5e3269d537",
                };
            });
        });
    }

    const startNewChat = async (event: any) => {
        const username = event.target.value;

        if (event.keyCode != 13 || !username)  // not Enter key or empty input
            return

        // wait for stomp connection
        if (!isConnected)
            return;


        if (!/^[a-zA-Z0-9]+$/.test(username)) {  // check alphebet and numeric
            alert("Error: invalid username");
            return;
        }

        // chech user exists
        const res = await fetch(`${BACKEND_URL}/user?username=${username}`, { credentials: "include" });

        const resJson = await res.json();

        if (res.status != 200) {
            console.log("🔴 Server error");
            return;
        }
        else if (!resJson.user_exist) {
            alert("Error: user does not exist");
            return;
        }

        setReceiver(username);

        // adjust conversation list and active conversation
        // check if user in the list
        // if true, update active flag 
        if (username in userMap.current) {
            setConversationList(conversation => {
                return conversation.map((item: conversation) => {
                    return {
                        username: item["username"],
                        profilePictureUrl: "https://external-preview.redd.it/1mF2BkbuRUyI5Od8V7aTZDVS_Y8-GMWeT4zvv7e_IrI.jpg?auto=webp&s=6dd561c5c1c1d69de69a56c8afaf4d5e3269d537",
                        active: item["username"] == username ? true : false
                    };
                });
            });
        }
        // else, insert an new entry and update active flag
        else {
            setConversationList(conversation => {
                const newEntry = {
                    username: username,
                    profilePictureUrl: "https://external-preview.redd.it/1mF2BkbuRUyI5Od8V7aTZDVS_Y8-GMWeT4zvv7e_IrI.jpg?auto=webp&s=6dd561c5c1c1d69de69a56c8afaf4d5e3269d537",
                    active: true
                }
                const oldEntryList = conversation.map((item: conversation) => {
                    return {
                        username: item["username"],
                        profilePictureUrl: "https://external-preview.redd.it/1mF2BkbuRUyI5Od8V7aTZDVS_Y8-GMWeT4zvv7e_IrI.jpg?auto=webp&s=6dd561c5c1c1d69de69a56c8afaf4d5e3269d537",
                        active: false   // all inactive
                    };
                })
                return [newEntry].concat(oldEntryList);
            });

            userMap.current[username] = true;
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

            {/* side bar */}
            <Drawer variant="permanent" sx={{
                width: "300px",
                flexShrink: 0,
                '& .MuiDrawer-paper': {
                    width: "300px",
                    boxSizing: 'border-box',
                },
            }}>
                <TextField
                    autoFocus={false}
                    label="Chat with someone"
                    placeholder="Search"
                    onKeyDown={startNewChat}
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
                    variant="outlined"
                />

                <List>
                    {
                        conversationList.map((item, idx) =>
                            <ListItemButton alignItems="flex-start" selected={receiver == item.username} onClick={ConversationOnClick(item.username)}>
                                <ListItemAvatar>
                                    {/* TODO: online status */}
                                    <Badge variant="dot" anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }} color="success" invisible={false} sx={{
                                        '& .MuiBadge-badge': {
                                            backgroundColor: '#32cd32',
                                            color: '#32cd32',
                                            boxShadow: `0 0 0 2px white`,
                                        },
                                    }} overlap="circular">
                                        <Avatar src={item.profilePictureUrl} />
                                    </Badge>
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

                                    // TODO: display latest message
                                    secondary={"latest message..."}
                                />
                                {/* TODO: unread message counts */}
                                {/* adjust Badge position: https://stackoverflow.com/questions/71399377/how-to-position-mui-badge-in-iconbutton-border-in-reactjs */}
                                <Badge badgeContent={4} color="error" invisible={false} style={{ transform: 'translate(0px, 25px)' }}></Badge>
                            </ListItemButton>
                        )
                    }
                </List>
            </Drawer>

            {/* chat contrainer */}
            {receiver ? <ChatRoom stompClient={stompClient.current} receiver={receiver} /> : <></>}
        </div>
    );
}

export default ChatLayout;
