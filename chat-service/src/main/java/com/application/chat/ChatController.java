package com.application.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;


@RestController
@RequestMapping(path = "/chat")
@Slf4j
public class ChatController {

	private final ChatService chatService;


	@Autowired
	public ChatController(ChatService chatService) {
		this.chatService = chatService;
	}

	/**
	 * Handle read event.
	 */
	@MessageMapping("/event/read/{receiver}")
	public void sendReadEvent(@DestinationVariable String receiver, Principal principal) throws Exception {
		String sender = principal.getName();

		chatService.updateConversationRead(sender, receiver, System.currentTimeMillis());
	}

}
