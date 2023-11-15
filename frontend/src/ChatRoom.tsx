import { useEffect, useLayoutEffect, useRef, useState } from 'react';
import { useAppSelector } from './redux/hooks';
import Stomp from 'stompjs';

// mui
import { AppBar, Avatar, Box, CircularProgress, IconButton, List, Skeleton, TextField, Toolbar, Typography } from '@mui/material';
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

/**
 * init: initial rendering (first fetch)
 * 
 * send: send new messages, scroll to bottom
 * 
 * receive: receive new messages, maintain current scroll position
 * 
 * normal: stable state, no scrolling control
 */
type LayoutState = "init" | "send" | "receive" | "normal";

interface props {
    stompClient: Stomp.Client;
    receiver: string;
    profilePictureUrl: string;
    lastRead: number;
}

/**
 * Check if two timestamp are the same date
 */
const isSameDate = (t1: number, t2: number) => {
    const D1 = new Date(t1);
    const D2 = new Date(t2);
    // console.log(D2.toLocaleDateString());   // ex: 8/17/2023

    return !(D1.toLocaleDateString() == D2.toLocaleDateString());
}

/**
 * Check if message is an image by extension. 
 */
function isImage(message: Message) {
    if (message.contentType == "file") {
        var filename = message.content.split(".");
        if (filename.length > 1)
            var file_type = filename[filename.length - 1].toLowerCase();

        if (imageExtension.includes(file_type))
            return true;
    }
    return false
}

/**
 * Convert object name to filename.
 * Skip folder name & conversation_id & random uuid.
 */
function objectNameToFilename(objectName: string) {
    return (objectName).split("/").slice(2).join().split("_").slice(1).join();
}

function ChatRoom({ stompClient, receiver, profilePictureUrl, lastRead }: props) {
    const [messageInput, setMessageInput] = useState("");
    const [messages, setMessages] = useState<Message[]>([]);    // from newest to earliest
    const [layoutState, setLayoutState] = useState<LayoutState>("init"); //  controll initial rendering & scroll adjusment
    const [fileDisabled, setFileDisabled] = useState(true); // disable file upload
    const [lockInput, setLockInput] = useState(false); // prevent send during composition event 
    const [isTop, setIsTop] = useState(false); // if the earliest message is reached
    const [lockFetch, setLockFetch] = useState(false); // prevent repetitive fetching
    const chatRoomRef = useRef(null);
    const [statusMessage, setStatusMessage] = useState("");
    const [fetchSet, setfetchSet] = useState(new Set());    // timestamps that are processed by fetchImages()

    const currentUserId = useAppSelector(state => state.login.userId); // Redux

    const { enqueueSnackbar, closeSnackbar } = useSnackbar();


    // control scroll position
    const [scrollHeight, setScrollHeight] = useState(1000);
    const [scrollTop, setScrollTop] = useState(0);

    const fetchAbortController = new AbortController(); // abort fetch request


    const handleSendClick = (message: string) => (event) => {
        if (message.length > 10000) {
            alert("🔴 Text size too large");
            return;
        }

        stompClient.send(`/app/private-message/${receiver}`, {}, JSON.stringify({ contentType: "text", content: message }));

        setMessageInput(""); // set input to empty
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

            stompClient.send(`/app/private-message/${receiver}`, {}, JSON.stringify({ contentType: "text", content: messageInput }));

            setMessageInput(""); // set input to empty
        }
    };


    // receive the echo of the message you sent
    const handleSendEcho = async (item: Message) => {
        // note: take the data from argument instead of state variable
        setMessages(messages => [{
            content: item.content,
            contentType: isImage(item) ? "image" : item.contentType,
            filename: objectNameToFilename(item.content),
            fileSize: item.fileSize,
            timestamp: item.timestamp,
            direction: "out",
            receiver: receiver,
            isHeadMessage: messages.length == 0 || isSameDate(messages[0].timestamp, item.timestamp),     // set divider flag
        }, ...messages]);

        setLayoutState("send");
    };


    const handleReceive = async (item: Message) => {
        // note: take the data from argument instead of state variable
        setMessages(messages => [{
            content: item.content,
            contentType: isImage(item) ? "image" : item.contentType,
            filename: objectNameToFilename(item.content),
            fileSize: item.fileSize,
            timestamp: item.timestamp,
            direction: "in",
            receiver: receiver,
            isHeadMessage: messages.length == 0 || isSameDate(messages[0].timestamp, item.timestamp),     // set divider flag
        }, ...messages]);

        setLayoutState("receive");

        // publish "read" event
        const body = {
            timestamp: new Date().getTime(),
        }
        stompClient.send(`/app/event/read/${receiver}`, {}, JSON.stringify(body));
    };


    const getStatusMessage = async (username: string) => {
        const params = new URLSearchParams({
            username: username,
        });

        try {
            var res = await fetch(`${BACKEND_URL}/user-data?${params}`, { credentials: "include", signal: fetchAbortController.signal });
        } catch (error) {
            return;
        }

        setStatusMessage((await res.json()).statusMessage);
    }


    /**
     * Fetch messages based on timestamp and page size. 
     * 
     * @param receiver Fetch messages with this username. 
     * @param timestamp Fetch message before this timestamp. 
     * @param pageSize The number of query rows. 
     */
    const fetchMessage = async (receiver: string, timestamp: number, pageSize: number) => {
        const params = new URLSearchParams({
            receiver: receiver,
            timestamp: timestamp.toString(),
            pageSize: pageSize.toString(),
        });

        try {
            var response = await fetch(`${BACKEND_URL}/chat/message?${params}`, { credentials: "include", signal: fetchAbortController.signal });
        } catch (error) {
            return;
        }

        let messageList: Message[] = await response.json();
        // console.log(messageList);

        // reach the earliest message
        if (messageList.length < pageSize) {
            var isTop = true;
            setIsTop(true);
        }

        // set divider for the last item in old List
        if (messageList.length == 0) {
            setMessages(oldList => oldList.map((item, index) => {
                return {
                    ...item,
                    isHeadMessage: index == oldList.length - 1 ? true : item.isHeadMessage,
                }
            }));
            return;
        }

        // response is in descending order (newest message first)
        messageList = messageList.map((item, index) => {
            return {
                content: item.content,
                contentType: isImage(item) ? "image" : item.contentType,
                filename: objectNameToFilename(item.content),
                fileSize: item.fileSize,
                timestamp: item.timestamp,
                direction: (item.sender == currentUserId) ? "out" : "in",
                receiver: receiver,
                isHeadMessage: index == messageList.length - 1 ? isTop : isSameDate(messageList[index].timestamp, messageList[index + 1].timestamp),     // set divider flag
            };
        });

        setMessages(oldList => oldList.concat(messageList));

    };

    /**
     * fetch images for new messages
     */
    const fetchImages = async () => {
        // check not-processed items
        const needFetch = messages.some((item, index) => !fetchSet.has(item.timestamp));

        if (!needFetch)
            return;

        // update fetchSet to cover all messages
        const newSet = new Set();
        messages.forEach(async (item, index) => { newSet.add(item.timestamp) });
        setfetchSet(newSet);

        messages.forEach(async (item, index) => {
            var object_url = item.content;

            // fetch new images
            if (!fetchSet.has(item.timestamp) && item.contentType == "image") {
                const params = new URLSearchParams({
                    filename: item.content,
                    receiver: receiver,
                });

                try {
                    var res = await fetch(`${BACKEND_URL}/chat/message/file/url?${params}`, { credentials: "include", signal: fetchAbortController.signal });
                } catch (error) {
                    return;
                }

                const s3objectUrl: String = await res.text();

                try {
                    res = await fetch(`${s3objectUrl}`, { signal: fetchAbortController.signal });
                } catch (error) {
                    return;
                }

                var fileBlob = await res.blob();
                object_url = URL.createObjectURL(fileBlob);

                const newItem = {
                    ...item,
                    content: object_url,
                } as Message;

                const timestamp = item.timestamp;

                // Note: locate item by timestamp, since index may change during other messages update
                setMessages(messages => messages.map((x) => x.timestamp == timestamp ? newItem : x));
            }

        })

    }

    /**
     * Initial message fetching. 
     */
    const initFetchMessage = async (receiver: string, timestamp: number, pageSize: number) => {
        setLockFetch(true);

        await fetchMessage(receiver, timestamp, pageSize);
        setLayoutState("normal");

        setFileDisabled(false);  // allow file upload
        setLockFetch(false);

    };


    /**
     * Synchronize the current scroll position. 
     * 
     * Note:
     *  zoom in/out might also trigger this event
     */
    const chatOnScroll = async () => {
        // update current position
        setScrollTop(x => chatRoomRef.current.scrollTop);

        // fetch new messages if scoll to top
        // skip if fetch locked, or still in initial state, or reached top
        if (!lockFetch && layoutState != "init" && !isTop && Math.abs(chatRoomRef.current.scrollTop) > chatRoomRef.current.scrollHeight - window.innerHeight * 2) {
            setLockFetch(true);

            await fetchMessage(receiver, messages[messages.length - 1].timestamp, pageSize);

            setFileDisabled(false);  // allow file upload
            setLockFetch(false);
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
        initFetchMessage(receiver, Number.MAX_SAFE_INTEGER, pageSize);
        setfetchSet(new Set());
        const { receiveSub, echoSub } = subscribeQueue();

        // publish "read" event
        const body = {
            timestamp: new Date().getTime(),
        }
        stompClient.send(`/app/event/read/${receiver}`, {}, JSON.stringify(body));

        return function cleanup() {
            // unsubscribe from queue
            receiveSub.unsubscribe();
            echoSub.unsubscribe();

            // abort targeted fetch requests
            fetchAbortController.abort();
        }

    }, [receiver]); // re-render when receiver change


    // see useLayoutEffect: https://react.dev/reference/react/useLayoutEffect
    // useLayoutEffect(() => {
    useEffect(() => {

        // console.log("❌ " + scrollTop)   // DEBUG
        // console.log("❌ " + scrollHeight)   // DEBUG
        // console.log("❌ " + chatRoomRef.current.scrollHeight)   // DEBUG
        // console.log("❌ " + layoutState)   // DEBUG
        // console.log(messages)
        // console.log(layoutState)

        // setTimeout(() => {
        //     console.log("✅ " + scrollTop)   // DEBUG
        //     console.log("✅ " + scrollHeight)   // DEBUG
        //     console.log("✅ " + chatRoomRef.current.scrollHeight)   // DEBUG
        //     console.log("✅ " + layoutState)   // DEBUG
        // }, 10);

        // update total height
        setScrollHeight(chatRoomRef.current.scrollHeight);

        if (layoutState == "receive") {
            if (scrollTop == 0)
                // keep position at bottom
                chatRoomRef.current.scrollTop = 0
            else
                // stay at the same view, consider the offset of the new messages
                chatRoomRef.current.scrollTop = scrollTop - (chatRoomRef.current.scrollHeight - scrollHeight);    // current - previous height
            setLayoutState("normal");
        } else if (layoutState == "send") {
            // scroll to bottom
            chatRoomRef.current.scrollTop = 0;
            setLayoutState("normal");
        }

    }, [messages]);

    // fetch image files after setting message list
    useEffect(() => {
        fetchImages();
    }, [messages])

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
            <div onScroll={chatOnScroll} ref={chatRoomRef} style={{ display: "flex", flexDirection: "column-reverse", flexGrow: 1, overflow: "auto" }}>

                {
                    layoutState == "init" ?
                        <Skeleton variant="rectangular" animation="wave" height="100%" />   // loading placeholder
                        :
                        <>
                            {/* add this empty block to fill up the empty space before messages */}
                            <Box sx={{ flexGrow: 1 }}></Box>

                            <List
                                sx={{
                                    padding: "10px",
                                    display: "flex",
                                    flexDirection: "column-reverse"
                                }}
                            >
                                {
                                    messages.map((item, index) => <MessageItem item={item} lastRead={lastRead} />)
                                }
                            </List>
                            {
                                !isTop &&
                                <div style={{ display: "flex", minHeight: "40px", justifyContent: "center", alignItems: "center" }}>
                                    {
                                        lockFetch &&
                                        <CircularProgress color="secondary" size="25px" />
                                    }
                                </div>
                            }
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
                        disabled={layoutState == "init"}
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
