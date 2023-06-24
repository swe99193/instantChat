package com.application.chat;

import com.application.channel_mapping.ChannelMappingService;
import com.application.message_storage.Message;
import com.application.message_storage.MessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
public class ChatController {

	@Autowired private SimpMessagingTemplate messagingTemplate;
	@Autowired
	private ChatService chatService;
	@Autowired
	private MessageService messageService;
	@Autowired
	private ChannelMappingService channelMappingService;

// private message
	@MessageMapping("/private-message/{receiver}")
	public void privateMessage(@DestinationVariable String receiver, ChatMessage message, Principal principal) throws Exception {
		System.out.println("HIT api: /private-message");
		System.out.println("session user: " + principal.getName());	// get sender name
		System.out.println("receiver: " + receiver);	// get sender name

		ChatMessage chatMessage = new ChatMessage(String.format("%s", message.getContent()));

		// not working
//		messagingTemplate.convertAndSendToUser(receiver + "/" + principal.getName(), "/queue/private", greetingMessage);
//		messagingTemplate.convertAndSendToUser(receiver, principal.getName() + "/queue/private", greetingMessage);

		// Note: send to /user/{receiver}/queue/private.{sender}
		// send to receiver
		Map<String, Object> headers = new HashMap<>();
		headers.put("auto-delete", "true");
		messagingTemplate.convertAndSendToUser(receiver, "/queue/private." + principal.getName(), chatMessage, headers);
		// send to sender
		//		messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/private." + receiver, chatMessage);

		chatService.saveMessage(principal.getName(), receiver, message.getContent());

	}

	@GetMapping("/list-message")
	public List<Message> listMessage(@RequestParam String receiver, Principal principal){
		String channel_id = channelMappingService.findChannelId(principal.getName(), receiver);

		List<Message> messageList = messageService.listMessage(channel_id);
		return messageList;
//		return new ArrayList<Message>(); // test: empty list
	}

}
