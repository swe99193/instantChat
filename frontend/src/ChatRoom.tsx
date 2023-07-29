import React, { useRef, useState, useEffect } from 'react';
import logo from './logo.svg';

import "@chatscope/chat-ui-kit-styles/dist/default/styles.min.css";
import {
  ChatContainer,
  MessageList,
  Message,
  MessageInput,
  MessageModel,
  Avatar,
  ConversationHeader,
} from "@chatscope/chat-ui-kit-react";
import Stomp from 'stompjs';
import { useAppSelector } from './redux/hooks';

const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;
const FRONTEND_URL = process.env.REACT_APP_FRONTEND_URL;

interface props{
    stompClient: Stomp.Client,
    receiver: string
}

function ChatRoom({ stompClient, receiver }: props) {
    const [msgInputValue, setMsgInputValue] = useState("");
    const [messages, setMessages] = useState<MessageModel[]>([]);
    const [inputDisabled, setInputDisabled] = useState(true);
    const currentUserId = useAppSelector(state => state.login.userId); // Redux

    const handleSend = (message: string) => {
        // setMessages(messages => [...messages, {
        //   message,
        //   sentTime: new Date().toLocaleString('zh-Hans-CN').slice(0,-3),
        //   sender: "none",
        //   direction: "outgoing",
        //   position:"single"
        // }]);
        setMsgInputValue(""); // set to empty after send
        stompClient.send(`/app/private-message/${receiver}`, {}, JSON.stringify({'content': message}));
      };

    // receive the echo of the message you sent
    const handleSendEcho = (message: string) => {
        setMessages(messages => [...messages, {
          message,
          sentTime: new Date().toLocaleString('zh-Hans-CN').slice(0,-3),
          sender: "none",
          direction: "outgoing",
          position:"single"
        }]);
      };
    
      const handleReceive = (message: string) => {
        setMessages(messages => [...messages, {
          message,
          sentTime: new Date().toLocaleString('zh-Hans-CN').slice(0,-3),
          sender: receiver,
          direction: "incoming",
          position:"single"
        }]);
      };

      const listMessage = async () => {

        const response = await fetch(`${BACKEND_URL}/list-message?receiver=${receiver}`, { credentials: "include" });
        // const response = await fetch(`http://localhost:8080/what`, { credentials: "include" });

        let messageList = await response.json();
        // console.log(messageList);

        setMessages(messages =>{
            let arr = messageList.map((message: { content: string; sender: string; timestamp: number})=>{
                return {
                    message: message.content,
                    sentTime: new Date(message.timestamp).toLocaleString('zh-Hans-CN').slice(0,-3),
                    sender: message.sender,
                    direction: (message.sender == receiver) ? "incoming" : "outgoing",
                    position: "single"
                };
            });
            return arr;   // overwrite "messages"
        });

        setInputDisabled(false);  // allow input
      };

      const returnMainPage = () => {
        window.location.assign(`${FRONTEND_URL}`); // redirect to main page, don't use "replace"
      }
    
      const subscribeQueue = () => {
        const receiveSub = stompClient.subscribe(`/user/queue/private.${receiver}`, function (message) {
          handleReceive(JSON.parse(message.body).content);
        }, { "auto-delete": true });

        // TODO: replace localStorage with Redux store
        const echoSub = stompClient.subscribe(`/user/queue/private.${receiver}-${currentUserId}`, function (message) {
          handleSendEcho(JSON.parse(message.body).content);
        }, { "auto-delete": true });

        return { receiveSub, echoSub };
      }

      useEffect(()=>{
        setInputDisabled(true);  // disable input
        setMessages([]);  // clear message
        setMsgInputValue(""); // clear input bar
        listMessage();
        const { receiveSub, echoSub } = subscribeQueue();

        return function cleanup () {
          // unsubscribe from queue
          receiveSub.unsubscribe();
          echoSub.unsubscribe();
        }

      }, [receiver]); // re-render when receiver change
      
      return (
            <ChatContainer>       
              <ConversationHeader>
                <ConversationHeader.Back onClick={returnMainPage}/>
                <Avatar src="https://external-preview.redd.it/1mF2BkbuRUyI5Od8V7aTZDVS_Y8-GMWeT4zvv7e_IrI.jpg?auto=webp&s=6dd561c5c1c1d69de69a56c8afaf4d5e3269d537" name={receiver} />
                <ConversationHeader.Content userName={receiver} info="Just a new user" />
              </ConversationHeader>
              
              <MessageList>

                { 
                  messages.map((m, i) => 
                  <Message key={i} model={m}>
                    <Message.Header sentTime={m.sentTime}></Message.Header> 
                    <Avatar src="https://external-preview.redd.it/1mF2BkbuRUyI5Od8V7aTZDVS_Y8-GMWeT4zvv7e_IrI.jpg?auto=webp&s=6dd561c5c1c1d69de69a56c8afaf4d5e3269d537" name="none" status="available" active={true} />
                  </Message>) 
                }
    
              </MessageList>
              <MessageInput placeholder="Type message here" onSend={handleSend} onChange={setMsgInputValue} value={msgInputValue} disabled={inputDisabled}/>        
            </ChatContainer>
      );
}

export default ChatRoom;
