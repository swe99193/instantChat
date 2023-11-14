export interface NewMessageEvent {
    content?: string;
    timestamp?: number;
    sender?: string;
    receiver?: string;
}