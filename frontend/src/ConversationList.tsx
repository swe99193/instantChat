// mui
import { Avatar, Badge, List, ListItemAvatar, ListItemButton, ListItemText, Typography } from "@mui/material";

// types
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
                    <ListItemButton alignItems="flex-start" selected={receiver == item.receiver} onClick={conversationOnClick(item.receiver)} sx={{ borderRadius: "5px", margin: "3px 5px" }}>
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
                                    sx={{ fontSize: "14px" }}
                                    component="div"
                                    color="text.primary"
                                    noWrap
                                >
                                    {item.receiver}
                                </Typography>
                            }

                            secondary={
                                <Typography
                                    sx={{ fontSize: "12px", color: "grey" }}
                                    component="div"
                                    noWrap
                                >
                                    {item.latestMessage}
                                </Typography>
                            }
                        />
                        {/* adjust Badge position: https://stackoverflow.com/questions/71399377/how-to-position-mui-badge-in-iconbutton-border-in-reactjs */}
                        <Badge badgeContent={item.unreadCount} color="error" invisible={false} style={{ transform: 'translate(0px, 25px)' }}></Badge>
                    </ListItemButton>
                )
            }
        </List>
    );
}

export default ConversationList;