package com.application.chat;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;


@Controller
public class ChatController {

	@Autowired private SimpMessagingTemplate messagingTemplate;
	@Autowired
	private ChatService chatService;

	// broadcast message
	@MessageMapping("/hello")
	@SendTo("/topic/greetings")
	public ChatMessage greeting(HelloMessage message) throws Exception {
//		Thread.sleep(1000); // simulated delay
		return new ChatMessage("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
	}


// private message
	@MessageMapping("/private-message/{receiver}")
	public void privateMessage(@DestinationVariable String receiver, ChatMessage message, Principal principal) throws Exception {
		System.out.println("HIT api: /private-message");
		System.out.println("session user: " + principal.getName());	// get sender name
		System.out.println("receiver: " + receiver);	// get sender name

//		Thread.sleep(1000); // simulated delay
		ChatMessage chatMessage = new ChatMessage(String.format("(private) From: %s: %s", principal.getName(), message.getContent()));

//		messagingTemplate.convertAndSendToUser(receiver + "/" + principal.getName(), "/queue/private", greetingMessage);
//		messagingTemplate.convertAndSendToUser(receiver, principal.getName() + "/queue/private", greetingMessage);
		messagingTemplate.convertAndSendToUser(receiver, "/queue/private." + principal.getName(), chatMessage);
		messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/private." + receiver, chatMessage);

		// send to /user/{username}/queue/private
	}

}
