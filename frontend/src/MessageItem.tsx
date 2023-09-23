/**
 * Message is an row element displaying avatar, message content, timestamp, etc
 * 
 */

import { useState } from "react";

// mui
import { Dialog, DialogContent, Divider, IconButton, ListItem, Paper, Stack, Typography } from "@mui/material";

// mui icons
import { Close, FilePresent } from "@mui/icons-material";

// types
import { Message } from "./types/Message.types";

// notification stack
import { useSnackbar } from 'notistack';


const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;

// TODO: read flag

interface props {
    item: Message;
}

function MessageItem({ item }: props) {
    const [open, setOpen] = useState(false);
    const [downloadDisabled, setDownloadDisabled] = useState(false); // avoid duplicate clicks

    const { enqueueSnackbar, closeSnackbar } = useSnackbar()

    const downloadFile = async () => {
        setDownloadDisabled(true);
        const snackbarId = enqueueSnackbar('Download pending', { variant: "info", autoHideDuration: 3000 });

        const params = new URLSearchParams({
            filename: item.content,
            receiver: item.receiver,
        });

        // download file
        const res = await fetch(`${BACKEND_URL}/message/file?${params}`, { credentials: "include" });
        var fileBlob = await res.blob();
        var object_url = URL.createObjectURL(fileBlob);

        // create temorary element for file object
        var fileElement = document.createElement("a");
        fileElement.href = object_url;
        fileElement.download = item.filename;
        fileElement.click();
        fileElement.remove();

        setDownloadDisabled(false);
        closeSnackbar(snackbarId);  // close previous message

        if (res.status != 200) {
            enqueueSnackbar('Download failed', { variant: "error", autoHideDuration: 3000 });
            return;
        }

    }

    return (
        <>

            {
                // Date divider
                item.isHeadMessage &&
                <Divider sx={{ fontSize: "12px", color: "grey", margin: "10px 0px" }}>
                    {new Date(item.timestamp).toLocaleDateString('zh-Hans-CN').split(" ")[0]}
                </Divider>
            }

            <ListItem sx={{ justifyContent: item.direction == "in" ? "left" : "right", padding: "5px", alignItems: "flex-end" }}>
                {/* justifyContent: left or right side*/}
                {/* alignItems: make avatar positioned at the bottom */}

                {
                    // outgoing message  (timestamp)
                    item.direction == "out" &&

                    <Typography
                        sx={{
                            fontSize: "11px",
                            color: "grey",
                        }}
                    >
                        {
                            // only display hour & minute
                            new Date(item.timestamp).toLocaleString('zh-Hans-CN').split(" ")[1].slice(0, -3)
                        }
                    </Typography>
                }

                {
                    // text content
                    item.contentType == "text" &&
                    <Paper
                        elevation={0}
                        sx={{
                            display: "flex",
                            maxWidth: "50%",
                            background: item.direction == "out" ? "#1E90FF" : "#E3E7ED",  // blue; light grey + a little blue
                            padding: "7px 15px",
                            margin: "0px 10px",
                            borderRadius: "20px",
                            fontSize: "14px",
                            color: item.direction == "out" ? "white" : "black",
                            wordBreak: "break-word", // prevent long word overflow
                            // wordWrap: "break-word", // (not working)
                            whiteSpace: "pre-wrap",
                        }}
                    >
                        {item.content}
                    </Paper>
                }
                {
                    // image content
                    // style: see object-fit
                    item.contentType == "image" &&
                    <>
                        <div style={{ maxWidth: "50%", margin: "0px 10px", overflow: "hidden", borderRadius: "20px", cursor: "pointer" }} onClick={() => setOpen(true)}>
                            {/* remove extra space: https://stackoverflow.com/questions/10844205/html-5-strange-img-always-adds-3px-margin-at-bottom */}
                            <img style={{ objectFit: "cover", verticalAlign: "middle", minWidth: "50px", maxWidth: "400px", minHeight: "50px", maxHeight: "150px", padding: 0, margin: 0 }} src={item.content} ></img>
                        </div>

                        {/* popup */}
                        <Dialog open={open} onClose={() => setOpen(false)} maxWidth="md" PaperProps={{ sx: { width: "100%", height: "80%", borderRadius: "10px", background: "dimgrey" } }}>
                            <div>
                                <IconButton
                                    onClick={() => setOpen(false)}
                                    sx={{ color: "lightgray" }}
                                >
                                    <Close />
                                </IconButton>
                            </div>

                            <DialogContent sx={{ display: "flex", justifyContent: "center", alignItems: "center", padding: "0px 40px 40px 40px" }}>
                                <img style={{ maxWidth: "100%", maxHeight: "100%" }} src={item.content}></img>
                            </DialogContent>
                        </Dialog>
                    </>
                }
                {
                    // file content
                    item.contentType == "file" &&
                    <Paper
                        elevation={0}
                        sx={{
                            display: "flex",
                            flexDirection: "row",
                            alignItems: "center",
                            maxWidth: "50%",
                            background: "WhiteSmoke",
                            padding: "7px 20px 7px 10px",
                            margin: "0px 10px",
                            borderRadius: "20px",
                            fontSize: "14px",
                            fontWeight: "bold",
                            wordBreak: "break-word", // prevent long word overflow
                            // wordWrap: "break-word", // (not working)
                        }}
                    >
                        <IconButton disableTouchRipple sx={{ color: "#404040" }} onClick={downloadFile} disabled={downloadDisabled}>
                            <FilePresent fontSize="medium" />
                        </IconButton>

                        <Stack>
                            <Typography
                                sx={{
                                    fontSize: "14px",
                                    fontWeight: "bold",

                                }}
                            >
                                {item.filename}
                            </Typography>
                            <Typography
                                sx={{
                                    fontSize: "14px",
                                    color: "grey",
                                }}
                            >
                                {item.fileSize > 1000000 ? Math.round(item.fileSize / 1000000).toString() + "MB" : item.fileSize > 1000 ? Math.round(item.fileSize / 1000).toString() + "KB" : Math.round(item.fileSize).toString() + " bytes"}
                            </Typography>
                        </Stack>
                    </Paper>
                }

                {
                    // incoming message (avatar)
                    // item.direction == "in" &&
                    // <ListItemAvatar sx={{ minWidth: "0px" }}>
                    //     <Badge
                    //         variant="dot"
                    //         anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
                    //         color="success"
                    //         invisible={false}
                    //         sx={{
                    //             '& .MuiBadge-badge': {
                    //                 backgroundColor: '#32cd32',
                    //                 color: '#32cd32',
                    //                 boxShadow: `0 0 0 2px white`,
                    //             },
                    //         }}
                    //         overlap="circular"
                    //     >
                    //         <Avatar src={item.profilePictureUrl} sx={{ height: "30px", width: "30px" }} />
                    //     </Badge>
                    // </ListItemAvatar>
                }

                {
                    // incoming message (timestamp)
                    item.direction == "in" &&
                    <Typography
                        sx={{
                            fontSize: "11px",
                            color: "grey",
                        }}
                    >
                        {
                            // only display hour & minute
                            new Date(item.timestamp).toLocaleString('zh-Hans-CN').split(" ")[1].slice(0, -3)
                        }
                    </Typography>
                }
            </ListItem>

        </>
    )
}

export default MessageItem;