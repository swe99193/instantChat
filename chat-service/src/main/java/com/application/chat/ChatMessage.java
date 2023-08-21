package com.application.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@AllArgsConstructor
@Getter
@Setter
public class ChatMessage {

	private String content;
	private Long timestamp;

}
