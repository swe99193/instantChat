package com.application.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 *
 */
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class NewMessageEvent {
	public String content;
	public Long timestamp;

	public String sender;
	public String receiver;
}
