package com.application.message_storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Standard message format.
 */
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
}
