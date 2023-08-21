/**
 * Message is an row element displaying avatar, message content, timestamp, etc
 * 
 */

// mui
import { Avatar, Badge, Divider, ListItem, ListItemAvatar, Paper, Typography } from "@mui/material";


// TODO: read flag

function Message({ item }) {
    // FIXME: scroll will cause re-rendering of messages
    // console.log(item);

    return (
        <>

            {
                // Date divider
                item.isHeadMessage &&
                <Divider sx={{ fontSize: "12px", color: "grey" }}>
                    {new Date(item.timestamp).toLocaleDateString('zh-Hans-CN').split(" ")[0]}
                </Divider>
            }

            <ListItem sx={{ justifyContent: item.direction == "in" ? "left" : "right", padding: "5px", alignItems: "flex-end" }}>
                {/* justifyContent: left or right side*/}
                {/* alignItems: make avatar positioned at the bottom */}

                {
                    item.direction == "out" &&
                    <>
                        <Typography
                            sx={{
                                fontSize: "11px",
                                color: "grey",
                                margin: "0px 0px 0px 80px"
                            }}
                        >
                            {
                                // only display hour & minute
                                new Date(item.timestamp).toLocaleString('zh-Hans-CN').split(" ")[1].slice(0, -3)
                            }
                        </Typography>
                        <Paper
                            elevation={0}
                            sx={{
                                display: "flex",
                                background: "#1E90FF",  // blue
                                padding: "7px 15px",
                                margin: "0px 0px 0px 10px",
                                borderRadius: "20px",
                                fontSize: "14px",
                                color: "white",
                                wordBreak: "break-word", // prevent long word overflow
                                // wordWrap: "break-word", // (not working)
                            }}
                        >
                            {item.content}
                        </Paper>
                    </>
                }

                {
                    item.direction == "in" &&
                    <ListItemAvatar sx={{ minWidth: "0px" }}>
                        {/* TODO: online status */}
                        <Badge
                            variant="dot"
                            anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
                            color="success"
                            invisible={false}
                            sx={{
                                '& .MuiBadge-badge': {
                                    backgroundColor: '#32cd32',
                                    color: '#32cd32',
                                    boxShadow: `0 0 0 2px white`,
                                },
                            }}
                            overlap="circular"
                        >
                            <Avatar src={item.profilePictureUrl} sx={{ height: "30px", width: "30px" }} />
                        </Badge>
                    </ListItemAvatar>
                }


                {
                    item.direction == "in" &&
                    <>
                        <Paper
                            elevation={0}
                            sx={{
                                background: "#E3E7ED",  // light grey + a little blue
                                padding: "7px 10px",
                                margin: "0px 10px",
                                borderRadius: "20px",
                                fontSize: "14px",
                                wordBreak: "break-word",
                            }}>
                            {item.content}
                        </Paper>
                        <Typography
                            sx={{
                                fontSize: "11px",
                                color: "grey",
                                margin: "0px 80px 0px 0px"
                            }}
                        >
                            {
                                // only display hour & minute
                                new Date(item.timestamp).toLocaleString('zh-Hans-CN').split(" ")[1].slice(0, -3)
                            }
                        </Typography>
                    </>
                }

                {

                }
            </ListItem>

        </>
    )
}

export default Message;