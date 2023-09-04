import { useEffect, useRef, useState } from 'react';

import Stomp from 'stompjs';
import { useAppSelector } from './redux/hooks';

// mui
import { AppBar, Avatar, IconButton, List, Skeleton, TextField, Toolbar, Typography } from '@mui/material';
import { Stack } from '@mui/system';

// mui icons
import { AttachFile, SendRounded } from '@mui/icons-material';

// components
import MessageItem from './MessageItem';

// types
import { Message } from './types/Message.types';

// notification stack
import { useSnackbar } from 'notistack';


const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;

const image_extension = ["jpeg", "jpg", "gif", "png",];

interface props {
    stompClient: Stomp.Client;
    receiver: string;
}

function ChatRoom({ stompClient, receiver }: props) {
    const [messageInput, setMessageInput] = useState("");
    const [messages, setMessages] = useState<Message[]>([]);
    const [initState, setInitState] = useState(true); // disable text send
    const [fileDisabled, setFileDisabled] = useState(true); // disable file upload
    const chatRoomRef = useRef(null);

    const currentUserId = useAppSelector(state => state.login.userId); // Redux

    const { enqueueSnackbar, closeSnackbar } = useSnackbar();


    // control scroll position
    const [scrollHeight, setScrollHeight] = useState(1000);
    const [scrollTop, setScrollTop] = useState(1000);


    const handleSend = (message: string) => (event) => {
        if (message.length > 10000) {
            alert("🔴 Text size too large");
            return;
        }

        setMessageInput(""); // set input to empty
        stompClient.send(`/app/private-message/${receiver}`, {}, JSON.stringify({ contentType: "text", content: message }));
    };

    const onEnter = (event) => {
        if (!event.shiftKey && event.key == "Enter" && messageInput) {
            if (messageInput.length > 10000) {
                alert("🔴 Text size too large");
                return;
            }

            setMessageInput(""); // set input to empty
            stompClient.send(`/app/private-message/${receiver}`, {}, JSON.stringify({ contentType: "text", content: messageInput }));
        }
    };


    // receive the echo of the message you sent
    const handleSendEcho = async (message: Message) => {
        if (message.contentType == "file") {

            var _filename: string[] = message.content.split(".");
            if (_filename.length > 1)
                var file_type = _filename[_filename.length - 1].toLowerCase();

            // fetch image
            if (image_extension.includes(file_type)) {
                const res = await fetch(`${BACKEND_URL}/message/file?filename=${message.content}&receiver=${receiver}`, { credentials: "include" });
                var fileBlob = await res.blob();
                var object_url = URL.createObjectURL(fileBlob);
            }
        }

        // take the data from argument instead of state variable
        setMessages(messages => [...messages, {
            content: image_extension.includes(file_type) ? object_url : message.content,
            contentType: image_extension.includes(file_type) ? "image" : message.contentType,
            filename: message.content.substring(74),    // skip channel_id & random uuid
            fileSize: message.fileSize,
            timestamp: message.timestamp,
            direction: "out",
            receiver: receiver,
            profilePictureUrl: "https://external-preview.redd.it/1mF2BkbuRUyI5Od8V7aTZDVS_Y8-GMWeT4zvv7e_IrI.jpg?auto=webp&s=6dd561c5c1c1d69de69a56c8afaf4d5e3269d537",
            isHeadMessage: messages.length == 0 || isHeadMessage(messages[messages.length - 1].timestamp, message.timestamp),     // set divider flag
        }]);
    };


    const handleReceive = async (message: Message) => {
        if (message.contentType == "file") {
            var _filename: string[] = message.content.split(".");
            if (_filename.length > 1)
                var file_type = _filename[_filename.length - 1].toLowerCase();

            // fetch image
            if (image_extension.includes(file_type)) {
                const res = await fetch(`${BACKEND_URL}/message/file?filename=${message.content}&receiver=${receiver}`, { credentials: "include" });
                var fileBlob = await res.blob();
                var object_url = URL.createObjectURL(fileBlob);
            }
        }

        // take the data from argument instead of state variable
        setMessages(messages => [...messages, {
            content: image_extension.includes(file_type) ? object_url : message.content,
            contentType: image_extension.includes(file_type) ? "image" : message.contentType,
            filename: message.content.substring(74),    // skip channel_id & random uuid
            fileSize: message.fileSize,
            timestamp: message.timestamp,
            direction: "in",
            receiver: receiver,
            profilePictureUrl: "https://external-preview.redd.it/1mF2BkbuRUyI5Od8V7aTZDVS_Y8-GMWeT4zvv7e_IrI.jpg?auto=webp&s=6dd561c5c1c1d69de69a56c8afaf4d5e3269d537",
            isHeadMessage: messages.length == 0 || isHeadMessage(messages[messages.length - 1].timestamp, message.timestamp),     // set divider flag
        }]);
    };


    const isHeadMessage = (t1: number, t2: number) => {
        const D1 = new Date(t1);
        const D2 = new Date(t2);
        // console.log(D2.toLocaleDateString());   // ex: 8/17/2023

        return !(D1.toLocaleDateString() == D2.toLocaleDateString());   // not the same date
    }


    const listMessage = async () => {

        const response = await fetch(`${BACKEND_URL}/message?receiver=${receiver}`, { credentials: "include" });
        let messageList: Message[] = await response.json();
        // console.log(messageList);

        const arr = messageList.map(async (item, index) => {
            if (item.contentType == "file") {

                var _filename = item.content.split(".");
                if (_filename.length > 1)
                    var file_type = _filename[_filename.length - 1].toLowerCase();

                // fetch image
                if (image_extension.includes(file_type)) {
                    const res = await fetch(`${BACKEND_URL}/message/file?filename=${item.content}&receiver=${receiver}`, { credentials: "include" });
                    var fileBlob = await res.blob();
                    var object_url = URL.createObjectURL(fileBlob);
                }
            }

            return {
                content: image_extension.includes(file_type) ? object_url : item.content,
                contentType: image_extension.includes(file_type) ? "image" : item.contentType,
                filename: item.content.substring(74),    // skip channel_id & random uuid
                fileSize: item.fileSize,
                timestamp: item.timestamp,
                direction: (item.sender == currentUserId) ? "out" : "in",
                receiver: receiver,
                profilePictureUrl: "https://external-preview.redd.it/1mF2BkbuRUyI5Od8V7aTZDVS_Y8-GMWeT4zvv7e_IrI.jpg?auto=webp&s=6dd561c5c1c1d69de69a56c8afaf4d5e3269d537",
                isHeadMessage: index == 0 || isHeadMessage(messageList[index - 1].timestamp, messageList[index].timestamp),     // set divider flag
            } as Message;
        });

        setMessages(await Promise.all(arr));

        setInitState(false);  // allow text input
        setFileDisabled(false);  // allow file upload
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

        const echoSub = stompClient.subscribe(`/user/queue/private.${receiver}-${currentUserId}`, function (message) {
            handleSendEcho(JSON.parse(message.body));
        }, { "auto-delete": true });

        return { receiveSub, echoSub };
    }


    const attachmentOnChange = async (event) => {
        const file: File = event.target.files[0];

        event.target.value = null;  // reset file input

        if (file.size > 100000000) {
            alert("🔴 File size larger than 100MB not supported");
            return;
        }

        const data = new FormData();    // multipart/form-data
        data.append("contentType", "file");
        data.append("file", file);

        setFileDisabled(true); // disable temporarily
        const snackbarId = enqueueSnackbar('Upload pending', { variant: "info", autoHideDuration: 3000 });

        // send files by POST 
        const res = await fetch(`${BACKEND_URL}/private-message/file?receiver=${receiver}`, {
            method: "POST",
            credentials: "include",
            body: data,
        });

        closeSnackbar(snackbarId);  // close previous message
        setFileDisabled(false);

        if (res.status != 200) {
            enqueueSnackbar('Upload failed', { variant: "error", autoHideDuration: 3000 });
            return;
        }

        enqueueSnackbar('Upload success', { variant: "success", autoHideDuration: 3000 });
    }


    useEffect(() => {
        setInitState(true);  // disable input
        setFileDisabled(true);  // disable input
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

        setTimeout(() => {
            // scroll to bottom
            chatRoomRef.current.scrollTop = chatRoomRef.current.scrollHeight
        }, 10); // wait for image rendering to get the correct scrollHeight

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
                {
                    initState ?
                        <Skeleton variant="rectangular" animation="wave" height="100%" />   // loading placeholder
                        :
                        <List
                            sx={{
                                padding: "10px"
                            }}
                        >
                            {
                                messages.map((item, index) => <MessageItem item={item} />)
                            }
                        </List>
                }
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

                    {/* attachment button */}
                    <IconButton
                        disabled={fileDisabled}
                        component="label"
                        aria-label="send"
                        disableRipple
                        sx={{
                            color: "#1E90FF",    // blue
                            "&.Mui-disabled": {
                                // color: "grey",
                            },
                        }}
                    >
                        <AttachFile />
                        <input
                            type="file"
                            hidden
                            onChange={attachmentOnChange}
                        />
                    </IconButton>

                    {/* text input */}
                    <TextField
                        value={messageInput}
                        onChange={(event) => { if (event.target.value != "\n") setMessageInput(event.target.value) }}   // skip initial Enter key
                        onKeyDown={onEnter}
                        inputRef={input => input && input.focus()}      // note: mui autofocus not working
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
                        disabled={initState || messageInput.length == 0}
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
