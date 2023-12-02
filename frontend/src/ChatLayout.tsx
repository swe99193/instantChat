import { useEffect, useRef, useState } from 'react';
import { Client, StompSubscription } from '@stomp/stompjs';

// redux
import { useAppSelector } from './redux/hooks';

// mui
import { Avatar, Badge, Box, IconButton, InputAdornment, List, ListItemAvatar, ListItemButton, ListItemText, TextField, Typography } from '@mui/material';

// mui icons
import { Menu, Search } from '@mui/icons-material';

// components
import ChatRoom from './ChatRoom';
import NavigationDrawer from './NavigationDrawer';
import ConversationList from './ConversationList';

// utils
import { fetchProfilePicture } from './utils/fetchProfilePicture';

// types
import { Conversation } from './types/Conversation.types';
import { NewMessageEvent } from './types/NewMessageEvent.types';
import { ReadEvent } from './types/ReadEvent.types';


const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;
// const BACKEND_URL = "http://localhost:8081";  // for local testing
// const BACKEND_URL_4 = "http://localhost:8084";  // for local testing
const WEBSOCKET_ENDPOINT = process.env.REACT_APP_WEBSOCKET_ENDPOINT;


function ChatLayout() {

    const [conversationList, setConversationList] = useState<Conversation[]>([]);
    const usernameSet = useRef(new Set()); // a set of username in conversation list
    const [receiver, setReceiver] = useState("");   // receiver of active conversation
    const [profilePictureUrl, setProfilePictureUrl] = useState(""); // object url of active conversation, passed to children components

    const stompClient = useRef(new Client({ brokerURL: `${WEBSOCKET_ENDPOINT}` }));
    const [isConnected, setIsConnected] = useState(false); // whether the Stomp client has established connection
    const [searchDisable, setSearchDisable] = useState(true); // disable search until conversation list is rendered

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

        const list: any[] = await res.json();

        const arr = list.map(async (item) => {
            const receiver = item.user1 == currentUserId ? item.user2 : item.user1;
            usernameSet.current.add(receiver);

            // fetch profile picture
            const objectUrl = await fetchProfilePicture(receiver);

            return {
                receiver: receiver,
                profilePictureUrl: objectUrl,
                latestMessage: item.latestMessage,
                latestTimestamp: item.latestTimestamp,
                lastRead: item.user2 == currentUserId ? item.lastReadUser1 : item.lastReadUser2,
                unreadCount: item.unreadCount,
            } as Conversation;
        });

        setConversationList(await Promise.all(arr));
        setSearchDisable(false);
    }

    const ConversationOnClick = (receiver: string) => (event) => {
        // wait for stomp connection
        if (!isConnected)
            return;

        setReceiver(receiver);
        setProfilePictureUrl(conversationList.filter(item => item.receiver == receiver)[0].profilePictureUrl);
    }


    const startNewChat = async (event: any) => {
        const receiver = event.target.value;

        if (event.key != "Enter" || !receiver)  // not Enter key or empty input
            return

        // wait for stomp connection
        if (!isConnected)
            return;


        if (!/^[a-zA-Z0-9]+$/.test(receiver)) {  // check alphebet and numeric
            alert("🔴 Error: invalid username");
            return;
        }
        // check if user in the list
        if (usernameSet.current.has(receiver)) {
            setReceiver(receiver);
            setProfilePictureUrl(conversationList.filter(item => item.receiver == receiver)[0].profilePictureUrl);
            return;
        }

        const params = new URLSearchParams({
            receiver: receiver
        });

        const res = await fetch(`${BACKEND_URL}/conversation?${params}`, {
            method: "POST",
            credentials: "include",
            headers: {
                'Content-type': 'application/json; charset=UTF-8',
            },
            body: JSON.stringify({}),
        });

        const resJson = await res.json();

        if (res.status == 400) {
            alert(`🔴 Error: ${resJson.message}`);
            return;
        } else if (res.status != 200) {
            alert("🔴 Server error");
            return;
        }

        // update username set
        usernameSet.current.add(receiver);

        const objectUrl = await fetchProfilePicture(receiver);

        // insert new entry
        const newEntry: Conversation = {
            receiver: receiver,
            profilePictureUrl: objectUrl,
            latestMessage: resJson.latestMessage,
            latestTimestamp: resJson.latestTimestamp,
            lastRead: resJson.user2 == currentUserId ? resJson.lastReadUser1 : resJson.lastReadUser2,
            unreadCount: 0,
        }

        setConversationList(conversations => {
            return [newEntry].concat(conversations);
        });

        // enter conversation
        setReceiver(receiver);
        setProfilePictureUrl(objectUrl);
    }

    const handleNewMessageEvent = async (message: NewMessageEvent) => {
        // update latest message & timestamp & unread count
        // update new conversation order
        var receiver;

        // hack: get new values
        setReceiver(_receiver => {
            receiver = _receiver;
            return _receiver;
        })

        if (message.sender == currentUserId) {
            // echo new message
            setConversationList(list => list.map((item, index) => {
                return {
                    ...item,
                    // update the target entry
                    latestMessage: item.receiver == message.receiver ? message.content : item.latestMessage,
                    latestTimestamp: item.receiver == message.receiver ? message.timestamp : item.latestTimestamp,
                    unreadCount: item.unreadCount   // NO change
                }
            }).sort((a, b) => { return b.latestTimestamp - a.latestTimestamp }));
        }
        else {
            // receive new message 

            // new conversation
            if (!usernameSet.current.has(message.sender)) {
                // update username set
                usernameSet.current.add(message.sender);
                setConversationList(list => list.concat([{
                    receiver: message.sender,
                    profilePictureUrl: "",
                    latestMessage: message.content,
                    latestTimestamp: message.timestamp,
                    lastRead: new Date().getTime(),
                    unreadCount: 1,
                }]).sort((a, b) => { return b.latestTimestamp - a.latestTimestamp }));


                // fetch profile picture
                const objectUrl = await fetchProfilePicture(message.sender);
                setConversationList(list => list.map((item) => {
                    return {
                        ...item,
                        // update the target entry
                        profilePictureUrl: item.receiver == message.sender ? objectUrl : item.profilePictureUrl,
                    };
                }));

            }

            else
                setConversationList(list => list.map((item, index) => {
                    return {
                        ...item,
                        // update the target entry
                        latestMessage: item.receiver == message.sender ? message.content : item.latestMessage,
                        latestTimestamp: item.receiver == message.sender ? message.timestamp : item.latestTimestamp,
                        // if active conversation, unreadCount <= 0
                        // else, unreadCount++
                        unreadCount: item.receiver == message.sender ? (message.sender == receiver ? 0 : item.unreadCount + 1) : item.unreadCount,
                    }
                }).sort((a, b) => { return b.latestTimestamp - a.latestTimestamp }));
        }
    }

    const handleReadEvent = async (event: ReadEvent) => {
        // affect rendering of read flag in chatroom

        if (event.sender == currentUserId) {
            // echo read event
            setConversationList(list => list.map((item, index) => {
                return {
                    ...item,
                    // update the target entry
                    // note: unreadCount = 0
                    lastRead: item.lastRead,
                    unreadCount: item.receiver == event.receiver ? 0 : item.unreadCount
                }
            }));
        } else {
            // others read event
            setConversationList(list => list.map((item, index) => {
                return {
                    ...item,
                    // update the target entry
                    // note: If read event's timestamp larger, override. Else, no change 
                    lastRead: item.receiver == event.sender && item.lastRead < event.timestamp ? event.timestamp : item.lastRead,
                    unreadCount: item.unreadCount
                }
            }));
        }

    }


    useEffect(() => {
        fetchConversation();

        var newMessageSub: StompSubscription;
        var readSub: StompSubscription;

        stompClient.current.reconnectDelay = 1;
        stompClient.current.heartbeatIncoming = 1000;
        stompClient.current.heartbeatOutgoing = 1000;
        stompClient.current.activate();
        stompClient.current.onDisconnect = (frame) => {
            console.log("😵 stomp client disconnected");
            console.log(frame);
        };

        stompClient.current.onWebSocketClose = (frame) => {
            console.log("😵 websocket closed");
            console.log(frame);
            setIsConnected(false);
        };

        stompClient.current.onConnect = (frame) => {
            console.log("✅ stomp client connected");

            setIsConnected(true);

            newMessageSub = stompClient.current.subscribe(`/user/queue/global.newmessage`, function (message) {
                console.log("new message event: " + message.body);
                handleNewMessageEvent(JSON.parse(message.body));
            });

            readSub = stompClient.current.subscribe(`/user/queue/global.read`, function (message) {
                console.log("read event: " + message.body);
                handleReadEvent(JSON.parse(message.body));
            });

        };

        return function cleanup() {
            // unsubscribe from queue
            newMessageSub.unsubscribe();
            readSub.unsubscribe();
        }
    }, []);


    return (
        <div style={{ "height": "100vh", display: "flex" }}>

            {/* side bar (navigation) */}
            <NavigationDrawer open={open} setOpen={setOpen} />

            {/* side bar (conversation) */}
            <Box sx={{
                minWidth: "300px",
                maxWidth: "300px",
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
                        disabled={searchDisable}
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
                <Box sx={{
                    overflow: "auto",
                    height: "100%"
                }}>
                    <ConversationList receiver={receiver} conversationList={conversationList} conversationOnClick={ConversationOnClick} />
                </Box>
            </Box>

            {/* chat contrainer */}
            {receiver ? <ChatRoom stompClient={stompClient.current} receiver={receiver} profilePictureUrl={profilePictureUrl} lastRead={conversationList.find(item => item.receiver == receiver).lastRead} stompClientConnected={isConnected} /> : <></>}
        </div>
    );
}

export default ChatLayout;
