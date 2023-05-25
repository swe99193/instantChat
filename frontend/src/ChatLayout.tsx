import React, { useRef, useState, useEffect } from 'react';
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
import ChatRoom from './ChatRoom';
import { useLocation } from 'react-router-dom';

function ChatLayout() {
  // const [msgInputValue, setMsgInputValue] = useState("");
  // const [messages, setMessages] = useState<MessageModel[]>([]);

  var { state } = useLocation();
  var receiver = state.receiver;

  const socket = new SockJS('http://localhost:8080/gs-guide-websocket');
  const stompClient = useRef<Stomp.Client>(Stomp.over(socket));
  // console.log(stompClient);

  return(
    <ChatRoom stompClient={stompClient.current} receiver={receiver}/>
  );

  // const handleSend = (message: string) => {
  //   setMessages(messages => [...messages, {
  //     message,
  //     sentTime: "just now",
  //     sender: "Joe",
  //     direction: "outgoing",
  //     position:"single"
  //   }]);
  //   setMsgInputValue(""); // set to empty after send
  //   stompClient.current.send(`/app/private-message/${receiver}`, {}, JSON.stringify({'content': message}));
  // };

  // const handleReceive = (message: string) => {
  //   setMessages(messages => [...messages, {
  //     message,
  //     sentTime: "just now",
  //     sender: "Joe",
  //     direction: "incoming",
  //     position:"single"
  //   }]);
  // };


  // useEffect(()=>{
  //   stompClient.current.connect({}, function (frame) {
  //     console.log('Connected: ' + frame);
  //     stompClient.current.subscribe(`/user/queue/private.${receiver}`, function (message) {
  //         console.log(message);
  //         handleReceive(JSON.parse(message.body).content);
  //     });
  // });
  // }, []);

  
  // return (
  //   <div style={{ position:"relative", height: "500px" }}>
  //     <MainContainer>
  //       <ChatContainer>       
  //         <MessageList>
  //           <Message model={{
  //                   message: "Hello my friend",
  //                   sentTime: "just now",
  //                   sender: "Joe",
  //                   direction: "outgoing",
  //                   position:"single"
  //                   }} />
  //           <Message model={{
  //                   message: "Hello my friend",
  //                   sentTime: "just now",
  //                   sender: "Joe",
  //                   direction: "incoming",
  //                   position:"first"
  //                   }} />
  //           <Message model={{
  //                   message: "Hello my friend",
  //                   sentTime: "just now",
  //                   sender: "Joe",
  //                   direction: "outgoing",
  //                   position:"first"
  //                   }} />
  //           {messages.map((m, i) => <Message key={i} model={m} />)}

  //           </MessageList>
  //         <MessageInput placeholder="Type message here" onSend={handleSend} onChange={setMsgInputValue} value={msgInputValue} />        
  //       </ChatContainer>
  //     </MainContainer>
  //   </div>
  // );
}

export default ChatLayout;
