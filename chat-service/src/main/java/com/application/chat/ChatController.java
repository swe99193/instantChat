package com.application.chat;

import com.application.message_storage.Message;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
	public void privateMessage(@DestinationVariable String receiver, IncomingMessage incomingMessage, Principal principal) throws Exception {

		if(incomingMessage.getContent().length() > 10000)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Text size too large");

		// not working
//		messagingTemplate.convertAndSendToUser(receiver + "/" + principal.getName(), "/queue/private", greetingMessage);
//		messagingTemplate.convertAndSendToUser(receiver, principal.getName() + "/queue/private", greetingMessage);

		Long timestamp = System.currentTimeMillis();

		// FIXME: comment out for dev
		// save message to DynamoDB
		chatService.saveMessage(principal.getName(), receiver, incomingMessage.getContent(), incomingMessage.getContentType(), null, timestamp);

		OutgoingMessage outgoingMessage = new OutgoingMessage(incomingMessage.getContentType(), incomingMessage.getContent(), null, timestamp);

		// Note: send to /user/{receiver}/queue/private.{sender}
		// send to receiver
		Map<String, Object> headers = new HashMap<>();
		headers.put("auto-delete", "true");
		headers.put("expires", System.currentTimeMillis() + 700);	// set expire time, adjust based on the latency of MQ

		// send to receiver
		// sent if it is NOT self-conversation
		if(!receiver.equals(principal.getName()))
			messagingTemplate.convertAndSendToUser(receiver, "/queue/private." + principal.getName(), outgoingMessage, headers);

		// send to sender
		messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/private." + receiver + "-" + principal.getName(), outgoingMessage, headers);
	}

	@GetMapping("/message")
	public List<Message> listMessage(@RequestParam String receiver, @RequestParam Long timestamp, @RequestParam Integer pageSize, Principal principal){

		return chatService.listMessage(principal.getName(), receiver, timestamp, pageSize);
	}

	@PostMapping("/private-message/file")
	public void privateMessageWithFile(@RequestParam String receiver, IncomingMessageFile incomingMessageFile, Principal principal) throws Exception {

		if(incomingMessageFile.getFile().getSize() > 100000000.0)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size larger than 100MB not supported");

		// set the file content
		MultipartFile file = incomingMessageFile.getFile();
		Long timestamp = System.currentTimeMillis();

		// FIXME: comment out for dev
		// S3 upload
		String objectName = chatService.saveFile(principal.getName(), receiver, file);

		// save message to DynamoDB
		chatService.saveMessage(principal.getName(), receiver, objectName, incomingMessageFile.getContentType(), file.getSize(), timestamp);

		OutgoingMessage outgoingMessage = new OutgoingMessage(incomingMessageFile.getContentType(), objectName, file.getSize(), System.currentTimeMillis());

		// Note: send to /user/{receiver}/queue/private.{sender}
		// send to receiver
		Map<String, Object> headers = new HashMap<>();
		headers.put("auto-delete", "true");
		headers.put("expires", System.currentTimeMillis() + 700);	// set expire time, adjust based on the latency of MQ

		// send to receiver
		// sent if it is NOT self-conversation
		if(!receiver.equals(principal.getName()))
			messagingTemplate.convertAndSendToUser(receiver, "/queue/private." + principal.getName(), outgoingMessage, headers);

		// send to sender
		messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/private." + receiver + "-" + principal.getName(), outgoingMessage, headers);
	}

	@GetMapping("/message/file")
	public byte[] getFile(@RequestParam String filename, @RequestParam String receiver, Principal principal, HttpServletResponse response) throws Exception {

		response.setHeader("Content-Disposition", "attachment; filename="+ filename);
		return chatService.getFile(filename, receiver, principal.getName());
	}

}
