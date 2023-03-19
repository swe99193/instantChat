package com.application;

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
	@MessageMapping("/private-message")
	public void privateMessage(HelloMessage message, Principal principal) throws Exception {
		System.out.println("/private-message HIT ...");
		System.out.println(principal.getName());	// get sender name

//		Thread.sleep(1000); // simulated delay
		Greeting greetingMessage = new Greeting("(private) Hello, (dummy name) !");
		messagingTemplate.convertAndSendToUser("userABC", "/queue/private", greetingMessage);
		// send to /user/{username}/queue/private
	}

}
