export interface Message {
    sender?: string;
    receiver?: string;
    content?: any;
    contentType?: "text" | "file" | "image";
    filename?: string;
    fileSize?: number;
    timestamp?: number,
    direction?: "in" | "out";
    profilePictureUrl?: string;
    isHeadMessage?: boolean;
}