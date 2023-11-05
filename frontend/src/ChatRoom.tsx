import { useEffect, useLayoutEffect, useRef, useState } from 'react';
import { useAppSelector } from './redux/hooks';
import Stomp from 'stompjs';

// mui
import { AppBar, Avatar, CircularProgress, IconButton, List, Skeleton, TextField, Toolbar, Typography } from '@mui/material';
import { Stack } from '@mui/system';

// mui icons
import { AttachFile, SendRounded } from '@mui/icons-material';

// components
import MessageItem from './MessageItem';

// types
import { Message } from './types/Message.types';

// notification stack
import { useSnackbar } from 'notistack';

import { imageExtension } from './shared/supportedFileExtension';


const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;
// const BACKEND_URL_1 = "http://localhost:8084";  // for local testing
const pageSize = 20;

type LayoutState = "init" | "fetch" | "send";

interface props {
    stompClient: Stomp.Client;
    receiver: string;
    profilePictureUrl: string;
}

const isHeadMessage = (t1: number, t2: number) => {
    const D1 = new Date(t1);
    const D2 = new Date(t2);
    // console.log(D2.toLocaleDateString());   // ex: 8/17/2023

    return !(D1.toLocaleDateString() == D2.toLocaleDateString());
}


function ChatRoom({ stompClient, receiver, profilePictureUrl }: props) {
    const [messageInput, setMessageInput] = useState("");
    const [messages, setMessages] = useState<Message[]>([]);
    const [layoutState, setLayoutState] = useState<LayoutState>("init"); //  controll initial rendering & scroll adjusment
    const [fileDisabled, setFileDisabled] = useState(true); // disable file upload
    const [lockInput, setLockInput] = useState(false); // prevent send during composition event 
    const [isTop, setIsTop] = useState(false); // if the earliest message is reached
    const [lockFetch, setLockFetch] = useState(false); // prevent repetitive fetching
    const chatRoomRef = useRef(null);

    const [statusMessage, setStatusMessage] = useState("");

    const currentUserId = useAppSelector(state => state.login.userId); // Redux

    const { enqueueSnackbar, closeSnackbar } = useSnackbar();


    // control scroll position
    const [scrollHeight, setScrollHeight] = useState(1000);
    const [scrollTop, setScrollTop] = useState(1000);


    const handleSendClick = (message: string) => (event) => {
        if (message.length > 10000) {
            alert("🔴 Text size too large");
            return;
        }

        setMessageInput(""); // set input to empty
        setLayoutState("send");

        stompClient.send(`/app/private-message/${receiver}`, {}, JSON.stringify({ contentType: "text", content: message }));
    };

    const onEnter = (event) => {
        // check composition event
        if (lockInput)
            return;

        // check "Shift + Enter" (add new line) and empty input
        if (!event.shiftKey && event.key == "Enter" && messageInput) {
            if (messageInput.length > 10000) {
                alert("🔴 Text size too large");
                return;
            }

            setMessageInput(""); // set input to empty
            setLayoutState("send");

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
            if (imageExtension.includes(file_type)) {
                const params = new URLSearchParams({
                    filename: message.content,
                    receiver: receiver,
                });
                const res = await fetch(`${BACKEND_URL}/chat/message/file?${params}`, { credentials: "include" });
                var fileBlob = await res.blob();
                var object_url = URL.createObjectURL(fileBlob);
            }
        }

        // take the data from argument instead of state variable
        setMessages(messages => [...messages, {
            content: imageExtension.includes(file_type) ? object_url : message.content,
            contentType: imageExtension.includes(file_type) ? "image" : message.contentType,
            filename: (message.content as string).split("/").slice(2).join().split("_").slice(1).join(),    // skip folder & channel_id & random uuid
            fileSize: message.fileSize,
            timestamp: message.timestamp,
            direction: "out",
            receiver: receiver,
            isHeadMessage: messages.length == 0 || isHeadMessage(messages[messages.length - 1].timestamp, message.timestamp),     // set divider flag
        }]);
    };


    const handleReceive = async (message: Message) => {
        if (message.contentType == "file") {
            var _filename: string[] = message.content.split(".");
            if (_filename.length > 1)
                var file_type = _filename[_filename.length - 1].toLowerCase();

            // fetch image
            if (imageExtension.includes(file_type)) {
                const params = new URLSearchParams({
                    filename: message.content,
                    receiver: receiver,
                });
                const res = await fetch(`${BACKEND_URL}/chat/message/file?${params}`, { credentials: "include" });
                var fileBlob = await res.blob();
                var object_url = URL.createObjectURL(fileBlob);
            }
        }

        // take the data from argument instead of state variable
        setMessages(messages => [...messages, {
            content: imageExtension.includes(file_type) ? object_url : message.content,
            contentType: imageExtension.includes(file_type) ? "image" : message.contentType,
            filename: (message.content as string).split("/").slice(2).join().split("_").slice(1).join(),    // skip folder & channel_id & random uuid
            fileSize: message.fileSize,
            timestamp: message.timestamp,
            direction: "in",
            receiver: receiver,
            isHeadMessage: messages.length == 0 || isHeadMessage(messages[messages.length - 1].timestamp, message.timestamp),     // set divider flag
        }]);
    };


    const getStatusMessage = async (username: string) => {
        const params = new URLSearchParams({
            username: username,
        });
        const res = await fetch(`${BACKEND_URL}/user-data?${params}`, { credentials: "include" });

        setStatusMessage((await res.json()).statusMessage);
    }


    /**
     * Fetch messages based on timestamp and page size. 
     * 
     * @param receiver Fetch messages with this username. 
     * @param timestamp Fetch messagew before this timestamp. 
     * @param pageSize The number of query rows. 
     */
    const fetchMessage = async (receiver: string, timestamp: number, pageSize: number) => {
        setLockFetch(true);

        const params = new URLSearchParams({
            receiver: receiver,
            timestamp: timestamp.toString(),
            pageSize: pageSize.toString(),
        });

        const response = await fetch(`${BACKEND_URL}/chat/message?${params}`, { credentials: "include" });
        let messageList: Message[] = await response.json();
        // console.log(messageList);

        // reach the earliest message
        if (messageList.length < pageSize) {
            var isTop = true;
            setIsTop(true);
        }

        // response is in descending order (newest message first), need to reverse array
        const arrPromise = messageList.reverse().map(async (item, index) => {
            if (item.contentType == "file") {

                var _filename = item.content.split(".");
                if (_filename.length > 1)
                    var file_type = _filename[_filename.length - 1].toLowerCase();

                // fetch image
                if (imageExtension.includes(file_type)) {
                    const params = new URLSearchParams({
                        filename: item.content,
                        receiver: receiver,
                    });
                    const res = await fetch(`${BACKEND_URL}/chat/message/file?${params}`, { credentials: "include" });
                    var fileBlob = await res.blob();
                    var object_url = URL.createObjectURL(fileBlob);
                }
            }

            return {
                content: imageExtension.includes(file_type) ? object_url : item.content,
                contentType: imageExtension.includes(file_type) ? "image" : item.contentType,
                filename: (item.content as string).split("/").slice(2).join().split("_").slice(1).join(),    // skip folder & channel_id & random uuid
                fileSize: item.fileSize,
                timestamp: item.timestamp,
                direction: (item.sender == currentUserId) ? "out" : "in",
                receiver: receiver,
                isHeadMessage: index == 0 ? isTop : isHeadMessage(messageList[index - 1].timestamp, messageList[index].timestamp),     // set divider flag
            } as Message;
        });

        const arr = await Promise.all(arrPromise);

        setMessages(_list => arr.concat(_list));

        setLayoutState("fetch");  // allow text input
        setFileDisabled(false);  // allow file upload
        setLockFetch(false);
    };


    /**
     * Synchronize the current scroll position. 
     * 
     * Note:
     *  zoom in/out might also trigger this event
     */
    const chatOnScroll = () => {
        // update current height
        setScrollTop(x => chatRoomRef.current.scrollTop);

        // fetch new messages if scoll to top
        // skip if fetch locked, or still in initial state, or reached top
        if (!lockFetch && layoutState != "init" && !isTop && chatRoomRef.current.scrollTop == 0) {
            fetchMessage(receiver, messages[0].timestamp, pageSize);
        }

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

        const echoSub = stompClient.subscribe(`/user/queue/private.echo.${receiver}`, function (message) {
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

        setFileDisabled(true);
        const snackbarId = enqueueSnackbar('Upload pending', { variant: "info", autoHideDuration: 3000 });


        // send files by POST 
        const params = new URLSearchParams({
            receiver: receiver,
        });

        const res = await fetch(`${BACKEND_URL}/chat/private-message/file?${params}`, {
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
        setLayoutState("init");  // disable input
        setFileDisabled(true);  // disable input
        setLockInput(false); // reset
        setIsTop(false);    // reset
        setLockFetch(false);    // reset
        setMessages([]);  // clear message
        setMessageInput(""); // clear input bar
        setStatusMessage(""); // clear status messaage
        getStatusMessage(receiver);
        fetchMessage(receiver, Number.MAX_SAFE_INTEGER, pageSize);
        const { receiveSub, echoSub } = subscribeQueue();

        return function cleanup() {
            // unsubscribe from queue
            receiveSub.unsubscribe();
            echoSub.unsubscribe();
        }

    }, [receiver]); // re-render when receiver change


    // adjust scroll position BEFORE rendering
    // see useLayoutEffect: https://react.dev/reference/react/useLayoutEffect
    useLayoutEffect(() => {

        // console.log("❌ " + chatRoomRef.current.scrollHeight)   // DEBUG

        setTimeout(() => {
            // wait for calculation of total height including images' height
            // console.log("✅ " + chatRoomRef.current.scrollHeight)   // DEBUG

            // update total height
            setScrollHeight(chatRoomRef.current.scrollHeight);
        }, 5);

        if (layoutState == "init" || layoutState == "send") {
            // scroll to bottom
            chatRoomRef.current.scrollTop = chatRoomRef.current.scrollHeight;
        } else {
            // stay at the same view after new messages are fetched
            chatRoomRef.current.scrollTop = chatRoomRef.current.scrollHeight - scrollHeight;    // current - previous height
        }

    }, [layoutState, messages]);


    return (
        <div style={{ display: "flex", width: "100%", flexDirection: "column", minWidth: 0 }}>
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
                    {/* TODO: online status */}
                    <Avatar
                        sx={{
                            margin: "0px",
                        }}
                        src={profilePictureUrl}
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
                            {statusMessage}
                        </Typography>
                    </Stack>
                </Toolbar>
            </AppBar>

            {/* chat room */}
            <div onScroll={chatOnScroll} ref={chatRoomRef} style={{ display: "flex", flexDirection: "column", flexGrow: 1, overflow: "auto" }}>

                {
                    layoutState == "init" ?
                        <Skeleton variant="rectangular" animation="wave" height="100%" />   // loading placeholder
                        :
                        <>
                            {
                                !isTop &&
                                <div style={{ display: "flex", minHeight: "40px", justifyContent: "center", alignItems: "center" }}>
                                    {
                                        lockFetch &&
                                        <CircularProgress color="secondary" size="25px" />
                                    }
                                </div>
                            }

                            <List
                                sx={{
                                    padding: "10px"
                                }}
                            >
                                {
                                    messages.map((item, index) => <MessageItem item={item} />)
                                }
                            </List>
                        </>
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
                            color: "DodgerBlue",    // blue
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
                        onCompositionStart={() => setLockInput(true)}
                        onCompositionEnd={() => setLockInput(false)}
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
                        disabled={layoutState == "init" || messageInput.length == 0}
                        aria-label="send"
                        disableRipple
                        onClick={handleSendClick(messageInput)}
                        sx={{
                            color: "DodgerBlue",    // blue
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
