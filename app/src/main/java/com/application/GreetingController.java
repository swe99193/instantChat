package com.application;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;


@Controller
public class GreetingController {

	@Autowired private SimpMessagingTemplate messagingTemplate;

	// broadcast message
	@MessageMapping("/hello")
	@SendTo("/topic/greetings")
	public Greeting greeting(HelloMessage message) throws Exception {
//		Thread.sleep(1000); // simulated delay
		return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
	}


// private message
	@MessageMapping("/private-message/{receiver}")
	public void privateMessage(@DestinationVariable String receiver, HelloMessage message, Principal principal) throws Exception {
		System.out.println("HIT api: /private-message");
		System.out.println("session user: " + principal.getName());	// get sender name
		System.out.println("receiver: " + receiver);	// get sender name

//		Thread.sleep(1000); // simulated delay
		Greeting greetingMessage = new Greeting(String.format("(private) From: %s: Hello, (dummy name) !", principal.getName()));
//		messagingTemplate.convertAndSendToUser(receiver + "/" + principal.getName(), "/queue/private", greetingMessage);
//		messagingTemplate.convertAndSendToUser(receiver, principal.getName() + "/queue/private", greetingMessage);
		messagingTemplate.convertAndSendToUser(receiver, "/queue/private." + principal.getName(), greetingMessage);

		// send to /user/{username}/queue/private
	}

}
