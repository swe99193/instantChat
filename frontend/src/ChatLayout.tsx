import React, { useRef, useState, useEffect } from 'react';
import "@chatscope/chat-ui-kit-styles/dist/default/styles.min.css";

import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import ChatRoom from './ChatRoom';
import { Navigate, useLocation } from 'react-router-dom';

const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;
const FRONTEND_URL = process.env.REACT_APP_FRONTEND_URL;

function ChatLayout() {

  var { state } = useLocation();
  if (state)
    var receiver = state.receiver;

  const socket = new SockJS(`${BACKEND_URL}/gs-guide-websocket`);
  const stompClient = useRef<Stomp.Client>(Stomp.over(socket));
  // console.log(stompClient);

  if(receiver == null)
    return (<Navigate to="/"/>)

  return(
    <ChatRoom stompClient={stompClient.current} receiver={receiver}/>
  );
}

export default ChatLayout;
