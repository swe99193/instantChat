import React, { useRef, useState, useEffect } from 'react';
import logo from './logo.svg';
import './App.css';
import "@chatscope/chat-ui-kit-styles/dist/default/styles.min.css";
import {
  MainContainer,
  ChatContainer,
  MessageList,
  Message,
  MessageInput,
  MessageModel,
} from "@chatscope/chat-ui-kit-react";
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

interface props{
    stompClient: Stomp.Client,
    receiver: string
}

function ChatRoom({ stompClient, receiver}: props) {
    const [msgInputValue, setMsgInputValue] = useState("");
    const [messages, setMessages] = useState<MessageModel[]>([]);


    const handleSend = (message: string) => {
        setMessages(messages => [...messages, {
          message,
          sentTime: "just now",
          sender: "Joe",
          direction: "outgoing",
          position:"single"
        }]);
        setMsgInputValue(""); // set to empty after send
        stompClient.send(`/app/private-message/${receiver}`, {}, JSON.stringify({'content': message}));
      };
    
      const handleReceive = (message: string) => {
        setMessages(messages => [...messages, {
          message,
          sentTime: "just now",
          sender: "Joe",
          direction: "incoming",
          position:"single"
        }]);
      };

      const listMessage = async () => {

        const response = await fetch(`http://localhost:8080/list-message?receiver=${receiver}`, { credentials: "include" });
        // const response = await fetch(`http://localhost:8080/what`, { credentials: "include" });

        let messageList = await response.json();
        console.log(messageList);

        setMessages(messages =>{
            let arr = (messageList.map((message: { content: string; sender: string; })=>{
                return {
                    message: message.content,
                    sentTime: "just now",
                    sender: "Joe",
                    direction: (message.sender == receiver) ? "incoming" : "outgoing",
                    position: "single"
                };
            }) as unknown) as MessageModel;
            return messages.concat(arr);
        });
      };
    
    
      useEffect(()=>{
        stompClient.connect({}, function (frame) {
          console.log('Connected: ' + frame);
          stompClient.subscribe(`/user/queue/private.${receiver}`, function (message) {
              console.log(message);
              handleReceive(JSON.parse(message.body).content);
          }, { "auto-delete": true });

          listMessage();
      });
      }, []);
    
      
      return (
        <div style={{ position:"relative", height: "500px" }}>
          <MainContainer>
            <ChatContainer>       
              <MessageList>

                {messages.map((m, i) => <Message key={i} model={m} />)}
    
                </MessageList>
              <MessageInput placeholder="Type message here" onSend={handleSend} onChange={setMsgInputValue} value={msgInputValue} />        
            </ChatContainer>
          </MainContainer>
        </div>
      );
}

export default ChatRoom;
