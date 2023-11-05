package com.application.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class OutgoingMessage {

	public String contentType;
	public String content;
	public Long fileSize;
	public Long timestamp;

	public String sender;
	public String receiver;
	/**
	 * Whether this message is an echoed message (echo messages are sent to yourself when you send messages to another user).
	 */
	public Boolean isEcho;
}
