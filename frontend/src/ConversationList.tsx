import { useEffect, useState } from "react";
import Stomp from 'stompjs';


// mui
import { Avatar, Badge, Box, Button, InputLabel, Link, List, ListItemAvatar, ListItemButton, ListItemText, Paper, Stack, TextField, Typography } from "@mui/material";

// types
import { NewMessage } from "./types/NewMessage.types";
import { Conversation } from "./types/Conversation.types";


interface props {
    /**
     * receiver of active conversation
     */
    receiver: string;
    conversationOnClick: any;
    conversationList: Conversation[];
}


function ConversationList({ receiver, conversationList, conversationOnClick }: props) {

    return (
        <List>
            {
                conversationList.map((item, idx) =>
                    <ListItemButton alignItems="flex-start" selected={receiver == item.username} onClick={conversationOnClick(item.username)} sx={{ borderRadius: "5px", margin: "3px 5px" }}>
                        <ListItemAvatar>
                            {/* TODO: online status */}
                            {/* <Badge variant="dot" anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }} color="success" invisible={false} sx={{
                                        '& .MuiBadge-badge': {
                                            backgroundColor: '#32cd32',
                                            color: '#32cd32',
                                            boxShadow: `0 0 0 2px white`,
                                        },
                                    }} overlap="circular"> */}
                            <Avatar src={item.profilePictureUrl} />
                            {/* </Badge> */}
                        </ListItemAvatar>
                        <ListItemText
                            primary={
                                <Typography
                                    sx={{ display: 'inline' }}
                                    component="span"
                                    variant="body2"
                                    color="text.primary"
                                >
                                    {item.username}
                                </Typography>}

                            secondary={item.latestMessage}
                        />
                        {/* TODO: unread message counts */}
                        {/* adjust Badge position: https://stackoverflow.com/questions/71399377/how-to-position-mui-badge-in-iconbutton-border-in-reactjs */}
                        {/* <Badge badgeContent={4} color="error" invisible={false} style={{ transform: 'translate(0px, 25px)' }}></Badge> */}
                    </ListItemButton>
                )
            }
        </List>
    );
}

export default ConversationList;