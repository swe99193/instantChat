package com.application.chat;

import com.application.config.KafkaConfig;
import com.application.message_storage.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
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
@RequestMapping(path = "/chat")
@Slf4j
public class ChatController {

	private final SimpMessagingTemplate messagingTemplate;
	private final ChatService chatService;

	private final KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	public ChatController(SimpMessagingTemplate messagingTemplate, ChatService chatService, KafkaTemplate<String, String> kafkaTemplate) {
		this.messagingTemplate = messagingTemplate;
		this.chatService = chatService;
		this.kafkaTemplate = kafkaTemplate;
	}


	/**
	 * Endpoint for sending text message.,
	 */
	@MessageMapping("/private-message/{receiver}")
	public void sendTextMessage(@DestinationVariable String receiver, IncomingMessage incomingMessage, Principal principal) throws Exception {

		if (incomingMessage.content.length() > 10000)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Text size too large");


		Long timestamp = System.currentTimeMillis();
		String sender = principal.getName();

		// save message to DynamoDB
		chatService.saveMessage(principal.getName(), receiver, incomingMessage.getContent(), incomingMessage.getContentType(), null, timestamp);

		OutgoingMessage outgoingMessage = new OutgoingMessage(incomingMessage.contentType, incomingMessage.content, null, timestamp, sender, receiver, true);

		// Kafka Producer ⬇️
		String topic = KafkaConfig.MESSAGE_TOPIC;

		// echo message to yourself
		String message = new ObjectMapper().writeValueAsString(outgoingMessage);
		String key = String.format("%s.%s", sender, receiver);
		log.info("✅ message before Kafka: ", message);
		kafkaTemplate.send(topic, key, message);

		// send message to another user
		if (!receiver.equals(sender)) {
			outgoingMessage.isEcho = false;
			message = new ObjectMapper().writeValueAsString(outgoingMessage);
			kafkaTemplate.send(topic, key, message);
		}
	}

	/**
	 * Listen for messages from Kafka, and route them to different websocket topics.
	 */
	@KafkaListener(topics = KafkaConfig.MESSAGE_TOPIC)
	public void messageListener(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, @Header(KafkaHeaders.RECEIVED_KEY) String key) throws JsonProcessingException {
		log.info("✅ message after Kafka: " + message);
		log.info("✅ topic: " + topic);
		log.info("✅ key: " + key);

		OutgoingMessage outgoingMessage = new ObjectMapper().readValue(message, OutgoingMessage.class);
		String sender = outgoingMessage.sender;
		String receiver = outgoingMessage.receiver;

		// send message to another user
		if(!outgoingMessage.isEcho)
			messagingTemplate.convertAndSendToUser(receiver, "/queue/private." + sender, outgoingMessage);
		// echo message to yourself
		else
			messagingTemplate.convertAndSendToUser(sender, "/queue/private.echo." + receiver, outgoingMessage);

		// not working
//		messagingTemplate.convertAndSendToUser(receiver + "/" + principal.getName(), "/queue/private", greetingMessage);
//		messagingTemplate.convertAndSendToUser(receiver, principal.getName() + "/queue/private", greetingMessage);
	}


	@GetMapping("/message")
	public List<Message> getMessage(@RequestParam String receiver, @RequestParam Long timestamp, @RequestParam Integer pageSize, Principal principal){
		String sender = principal.getName();

		return chatService.listMessage(sender, receiver, timestamp, pageSize);
	}

	/**
	 * Endpoint for sending file message.
	 */
	@PostMapping("/private-message/file")
	public void privateMessageWithFile(@RequestParam String receiver, IncomingMessageFile incomingMessageFile, Principal principal) throws Exception {

		if(incomingMessageFile.file.getSize() > 100000000.0)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size larger than 100MB not supported");

		// set the file content
		MultipartFile file = incomingMessageFile.file;
		Long timestamp = System.currentTimeMillis();
		String sender = principal.getName();

		// TODO: if an image, read height & width and store in DynamoDB

		// S3 upload
		String objectName = chatService.saveFile(sender, receiver, file);

		// save message to DynamoDB
		chatService.saveMessage(sender, receiver, objectName, incomingMessageFile.contentType, file.getSize(), timestamp);

		OutgoingMessage outgoingMessage = new OutgoingMessage(incomingMessageFile.contentType, objectName, file.getSize(), System.currentTimeMillis(), sender, receiver, true);

		// Kafka Producer ⬇️
		String topic = KafkaConfig.MESSAGE_TOPIC;

		// echo message to yourself
		String message = new ObjectMapper().writeValueAsString(outgoingMessage);
		String key = String.format("%s.%s", sender, receiver);
		log.info("✅ message before Kafka: ", message);
		kafkaTemplate.send(topic, key, message);

		// send message to another user
		if (!receiver.equals(sender)) {
			outgoingMessage.isEcho = false;
			message = new ObjectMapper().writeValueAsString(outgoingMessage);
			kafkaTemplate.send(topic, key, message);
		}
	}

	/**
	 * Download file from S3
	 * @see #getFileUrl
	 */
//	@GetMapping("/message/file")
	public byte[] getFile(@RequestParam String filename, @RequestParam String receiver, Principal principal, HttpServletResponse response) throws Exception {
		String sender = principal.getName();

		response.setHeader("Content-Disposition", "attachment; filename="+ filename);
		return chatService.getFile(filename, receiver, sender);
	}

	/**
	 * Get temporary file download url.
	 */
	@GetMapping("/message/file/url")
	public String getFileUrl(@RequestParam String filename, @RequestParam String receiver, Principal principal) throws Exception {
		String sender = principal.getName();

		return chatService.getPresignedUrl(filename, receiver, sender);
	}

}
