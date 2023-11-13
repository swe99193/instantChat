package com.application.conversation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * message format used in "new message" event
 */
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class NewMessage {
	public String content;
	public Long timestamp;

	public String sender;
	public String receiver;
}
