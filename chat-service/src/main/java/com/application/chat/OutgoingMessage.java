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

	private String contentType;
	private String content;
	private Long fileSize;
	private Long timestamp;

}
