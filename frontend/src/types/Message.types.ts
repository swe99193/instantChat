export interface Message {
    sender?: string;
    receiver?: string;
    content?: string;
    contentType?: "text" | "file" | "image";
    filename?: string;
    fileSize?: number;
    timestamp?: number,
    direction?: "in" | "out";
    profilePictureUrl?: string;
    /** If true, a date divider will be appended at the top of this message item.  */
    isHeadMessage?: boolean;
}