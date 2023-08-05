import React, { useRef, useState, useEffect } from 'react';
import "@chatscope/chat-ui-kit-styles/dist/default/styles.min.css";

import Stomp from 'stompjs';
import ChatRoom from './ChatRoom';
import { Avatar, Conversation, ConversationList, MainContainer, Sidebar } from '@chatscope/chat-ui-kit-react';
import { TextField } from '@mui/material';


const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;
const FRONTEND_URL = process.env.REACT_APP_FRONTEND_URL;
const WEBSOCKET_ENDPOINT = process.env.REACT_APP_WEBSOCKET_ENDPOINT;

interface userObject {
    username: string,
    chatUser: string
}

interface conversation {
    username: string,
    profilePictureUrl: string
    active: boolean
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

        const response = await fetch(`${BACKEND_URL}/conversation`, { credentials: "include" });

        let list: Array<any> = await response.json();

        setConversationList(conversation => {
            let arr = list.map((userObject: userObject) => {
                const username = userObject["chatUser"]
                // initialize 
                userMap.current[username] = true;

                return {
                    username: username,
                    profilePictureUrl: "https://external-preview.redd.it/1mF2BkbuRUyI5Od8V7aTZDVS_Y8-GMWeT4zvv7e_IrI.jpg?auto=webp&s=6dd561c5c1c1d69de69a56c8afaf4d5e3269d537",
                    active: false
                };
            });
            return conversation.concat(arr);
        });
    }

    const ConversationOnClick = (username: string) => {
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
                    active: item["username"] == username ? true : false
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
        const response = await fetch(`${BACKEND_URL}/user?username=${username}`, { credentials: "include" });

        let isUserExist: boolean = await response.json();

        if (!isUserExist) {
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
        <div style={{ "height": "95vh" }}>
            <MainContainer>
                <Sidebar position="left">
                    <TextField autoFocus={false} label="Chat with someone" placeholder="Search" onKeyDown={startNewChat} sx={{ "margin": "10px" }} size="small" > </TextField>
                    <ConversationList>
                        {
                            conversationList.map((item, idx) =>
                                <Conversation name={item.username} lastSenderName="" info="" onClick={() => ConversationOnClick(item.username)} active={item.active}>
                                    <Avatar src={item.profilePictureUrl} name={item.username} />
                                </Conversation>
                            )
                        }
                    </ConversationList>

                </Sidebar>
                {receiver ? <ChatRoom stompClient={stompClient.current} receiver={receiver} /> : <></>}
            </MainContainer>
        </div>
    );
}

export default ChatLayout;
