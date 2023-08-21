import { useEffect, useRef, useState } from 'react';

import Stomp from 'stompjs';
import { useAppSelector } from './redux/hooks';

// mui
import { AppBar, Avatar, IconButton, List, TextField, Toolbar, Typography } from '@mui/material';
import { Stack } from '@mui/system';

// mui icons
import { SendRounded } from '@mui/icons-material';

// components
import Message from './Message';


const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;

interface props {
    stompClient: Stomp.Client,
    receiver: string
}

function ChatRoom({ stompClient, receiver }: props) {
    const [messageInput, setMessageInput] = useState("");
    const [messages, setMessages] = useState<any>([]);
    const [inputDisabled, setInputDisabled] = useState(true);
    const [init, setInit] = useState(true);
    const currentUserId = useAppSelector(state => state.login.userId); // Redux
    const chatRoomRef = useRef(null);

    // control scroll position
    const [scrollHeight, setScrollHeight] = useState(1000);
    const [scrollTop, setScrollTop] = useState(1000);


    const handleSend = (message: string) => (event) => {
        setMessageInput(""); // set to empty after send
        stompClient.send(`/app/private-message/${receiver}`, {}, JSON.stringify({ 'content': message }));
    };

    // receive the echo of the message you sent
    const handleSendEcho = (message) => {
        setMessages(messages => [...messages, {
            content: message.content,
            timestamp: message.timestamp,
            sender: "none",
            direction: "out",
            position: "single",
            profilePictureUrl: "https://external-preview.redd.it/1mF2BkbuRUyI5Od8V7aTZDVS_Y8-GMWeT4zvv7e_IrI.jpg?auto=webp&s=6dd561c5c1c1d69de69a56c8afaf4d5e3269d537",

        }]);
    };

    const handleReceive = (message) => {
        setMessages(messages => [...messages, {
            content: message.content,
            timestamp: message.timestamp,
            sender: receiver,
            direction: "in",
            position: "single",
            profilePictureUrl: "https://external-preview.redd.it/1mF2BkbuRUyI5Od8V7aTZDVS_Y8-GMWeT4zvv7e_IrI.jpg?auto=webp&s=6dd561c5c1c1d69de69a56c8afaf4d5e3269d537",
        }]);
    };

    const isHeadMessage = (t1: number, t2: number) => {
        const D1 = new Date(t1);
        const D2 = new Date(t2);
        // console.log(D2.toLocaleDateString());   // ex: 8/17/2023

        return !(D1.toLocaleDateString() == D2.toLocaleDateString())
    }

    const listMessage = async () => {

        const response = await fetch(`${BACKEND_URL}/list-message?receiver=${receiver}`, { credentials: "include" });
        // const response = await fetch(`http://localhost:8080/what`, { credentials: "include" });

        let messageList = await response.json();
        // console.log(messageList);

        setMessages(messages => {
            let arr = messageList.map((item, index) => {
                return {
                    content: item.content,
                    timestamp: item.timestamp,
                    sender: item.sender,
                    direction: (item.sender == currentUserId) ? "out" : "in",
                    position: "single",
                    profilePictureUrl: "https://external-preview.redd.it/1mF2BkbuRUyI5Od8V7aTZDVS_Y8-GMWeT4zvv7e_IrI.jpg?auto=webp&s=6dd561c5c1c1d69de69a56c8afaf4d5e3269d537",
                    isHeadMessage: index == 0 || isHeadMessage(messageList[index - 1].timestamp, messageList[index].timestamp),
                };
            });
            return arr;   // overwrite "messages"
        });


        setInputDisabled(false);  // allow input
    };

    /**
     * synchronize the current scroll position
     * 
     * Note:
     *  zoom in/out might also trigger this event
     */
    const chatOnScroll = () => {
        // update current height
        setScrollTop(x => chatRoomRef.current.scrollTop);

        // console.log(chatRoomRef.current.scrollTop);
        // console.log(chatRoomRef.current.scrollHeight);
        // console.log(scrollTop);
        // console.log(scrollHeight);
    }

    const subscribeQueue = () => {
        // TODO: push "READ" to MQ

        const receiveSub = stompClient.subscribe(`/user/queue/private.${receiver}`, function (message) {
            handleReceive(JSON.parse(message.body));
        }, { "auto-delete": true });

        // TODO: replace localStorage with Redux store
        const echoSub = stompClient.subscribe(`/user/queue/private.${receiver}-${currentUserId}`, function (message) {
            handleSendEcho(JSON.parse(message.body));
        }, { "auto-delete": true });

        return { receiveSub, echoSub };
    }

    useEffect(() => {
        setInputDisabled(true);  // disable input
        setMessages([]);  // clear message
        setMessageInput(""); // clear input bar
        listMessage();
        const { receiveSub, echoSub } = subscribeQueue();

        return function cleanup() {
            // unsubscribe from queue
            receiveSub.unsubscribe();
            echoSub.unsubscribe();
        }

    }, [receiver]); // re-render when receiver change


    useEffect(() => {
        // update total height
        setScrollHeight(chatRoomRef.current.scrollHeight)

        chatRoomRef.current.scrollTop = chatRoomRef.current.scrollHeight

        // TODO?: control scroll position

        // if (scrollHeight) {
        // elementRef.current.scrollTop = 1000;
        // const scrollingElement = document.scrollingElement;
        // scrollingElement.scrollTop = 0;
        // window.scrollTo({ top: 2000 });
        // console.log(scrollingElement.scrollHeight);
        // elementRef.current.scrollIntoView();
        // }
    }, [messages]);


    return (
        <div style={{ display: "flex", flexGrow: 1, flexDirection: "column", minWidth: 0 }}>
            {/* note: adding overflow at this layer will cause the app bar to be scrollable */}

            {/* top bar */}
            <AppBar position="static"
                sx={{
                    width: "100%",
                    background: "white",
                    // boxShadow: "0px 0px 5px 0px black",
                }}
            >
                <Toolbar
                    sx={{
                        alignItems: "center"
                    }}
                >
                    <Avatar
                        sx={{
                            margin: "0px",
                        }}
                        src="https://external-preview.redd.it/1mF2BkbuRUyI5Od8V7aTZDVS_Y8-GMWeT4zvv7e_IrI.jpg?auto=webp&s=6dd561c5c1c1d69de69a56c8afaf4d5e3269d537"
                    />

                    <Stack direction="column">
                        <Typography
                            sx={{
                                color: "black",
                                margin: "0px 10px",
                            }}
                        >
                            {receiver}
                        </Typography>
                        <Typography
                            sx={{
                                color: "grey",
                                margin: "0px 10px",
                                fontSize: "12px"
                            }}
                        >
                            Good Luck!
                        </Typography>
                    </Stack>
                </Toolbar>
            </AppBar>

            {/* chat room */}
            <div onScroll={chatOnScroll} ref={chatRoomRef} style={{ display: "flex", flexDirection: "column", flexGrow: 1, overflow: "auto" }}>
                <List
                    sx={{
                        padding: "10px"
                    }}
                >
                    {
                        messages.map((item, index) => <Message item={item} />)
                    }
                </List>
            </div>

            {/* bottom bar */}
            <AppBar position="static"
                sx={{
                    width: "100%",
                    background: "white",
                    // boxShadow: "none",
                }}
            >
                <Toolbar
                    variant='dense'     // trick: set dense and override (remove) its minHeight
                    sx={{
                        alignItems: "flex-end",
                        padding: "5px",
                        minHeight: "0px"
                    }}
                >
                    {/* text input */}
                    <TextField
                        value={messageInput}
                        onChange={(event) => setMessageInput(event.target.value)}
                        disabled={inputDisabled}                        // enable when init completed
                        inputRef={input => input && input.focus()}      // mui autofocus not working
                        size="small"
                        autoComplete="off"
                        fullWidth
                        multiline
                        maxRows={10}    // set maximum visible rows
                        sx={{
                            // change border color
                            // https://stackoverflow.com/questions/52911169/how-to-change-the-border-color-of-material-ui-textfield
                            '& .MuiOutlinedInput-root': {
                                '& fieldset': {
                                    borderColor: 'white',
                                    border: "0",
                                },
                                '&:hover fieldset': {
                                    borderColor: 'white',
                                    border: "0",
                                },
                                "&.Mui-focused fieldset": {
                                    borderColor: "white",
                                    border: "0",
                                },
                            },
                        }}
                        InputProps={{
                            style: {
                                background: "#E3E7ED",   // light grey + a little blue
                                fontSize: "14px",
                                borderRadius: "20px",
                            }
                        }}
                    >

                    </TextField>

                    {/* send button */}
                    <IconButton
                        disabled={messageInput.length == 0}
                        aria-label="send"
                        disableRipple
                        onClick={handleSend(messageInput)}
                        sx={{
                            color: "#1E90FF",    // blue
                            "&.Mui-disabled": {
                                // color: "grey",
                            },
                        }}
                    >
                        <SendRounded />
                    </IconButton>
                </Toolbar>
            </AppBar>

        </div>
    );
}

export default ChatRoom;
