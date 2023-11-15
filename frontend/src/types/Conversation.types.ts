export interface Conversation {
    receiver: string,
    profilePictureUrl: string
    latestMessage: string;
    latestTimestamp: number;
    lastRead: number;
}
