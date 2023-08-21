package com.application.chat;

import com.application.message_storage.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
public class ChatController {

	private final SimpMessagingTemplate messagingTemplate;
	private final ChatService chatService;

	@Autowired
	public ChatController(SimpMessagingTemplate messagingTemplate, ChatService chatService) {
		this.messagingTemplate = messagingTemplate;
		this.chatService = chatService;
	}


	// private message
	@MessageMapping("/private-message/{receiver}")
	public void privateMessage(@DestinationVariable String receiver, ChatMessage message, Principal principal) throws Exception {
//		System.out.println("HIT api: /private-message");
//		System.out.println("session user: " + principal.getName());	// get sender name
//		System.out.println("receiver: " + receiver);	// get sender name

		ChatMessage chatMessage = new ChatMessage(String.format("%s", message.getContent()), System.currentTimeMillis());

		// not working
//		messagingTemplate.convertAndSendToUser(receiver + "/" + principal.getName(), "/queue/private", greetingMessage);
//		messagingTemplate.convertAndSendToUser(receiver, principal.getName() + "/queue/private", greetingMessage);

		chatService.saveMessage(principal.getName(), receiver, chatMessage);

		// Note: send to /user/{receiver}/queue/private.{sender}
		// send to receiver
		Map<String, Object> headers = new HashMap<>();
		headers.put("auto-delete", "true");
		headers.put("expires", System.currentTimeMillis() + 700);	// set expire time, adjust based on the latency of MQ

		messagingTemplate.convertAndSendToUser(receiver, "/queue/private." + principal.getName(), chatMessage, headers);
		// send to sender
		messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/private." + receiver + "-" + principal.getName(), chatMessage, headers);
	}

	@GetMapping("/list-message")
	public List<Message> listMessage(@RequestParam String receiver, Principal principal){

		return chatService.listMessage(principal.getName(), receiver);
	}

}
