package com.application.chat;

import com.application.config.KafkaConfig;
import com.application.message_storage.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(path = "/chat")
@Slf4j
public class ChatController {

	private final ChatService chatService;

	private final KafkaTemplate<String, String> kafkaTemplate;

	private static final List<String> IMAGE_EXTENSION = List.of("jpeg", "jpg", "gif", "png");

	@Autowired
	public ChatController(ChatService chatService, KafkaTemplate<String, String> kafkaTemplate) {
		this.chatService = chatService;
		this.kafkaTemplate = kafkaTemplate;
	}


	/**
	 * Endpoint for sending text message.,
	 */
	@MessageMapping("/private-message/{receiver}")
	@SubscribeMapping
	public void sendTextMessage(@DestinationVariable String receiver, IncomingMessage incomingMessage, Principal principal) throws Exception {

		if (incomingMessage.content.length() > 10000)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Text size too large");


		Long timestamp = System.currentTimeMillis();
		String sender = principal.getName();

		// save message to DynamoDB
		chatService.saveMessage(principal.getName(), receiver, incomingMessage.getContent(), incomingMessage.getContentType(), null, timestamp);

		// update conversation latest message and timestamp
		chatService.updateConversationLatestMessage(sender, receiver, incomingMessage.content, timestamp);

		OutgoingMessage outgoingMessage = new OutgoingMessage(incomingMessage.contentType, incomingMessage.content, null, timestamp, sender, receiver);

		// Kafka Producer ⬇️
		String topic = KafkaConfig.MESSAGE_TOPIC;
		String message = new ObjectMapper().writeValueAsString(outgoingMessage);
		String key = String.format("%s.%s", sender, receiver);
		kafkaTemplate.send(topic, key, message);
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
		String sender = principal.getName();

		// TODO: if an image, read height & width and store in DynamoDB

		// S3 upload
		String objectName = chatService.saveFile(sender, receiver, file);

		// set timestamp after file upload
		Long timestamp = System.currentTimeMillis();

		// save message to DynamoDB
		chatService.saveMessage(sender, receiver, objectName, incomingMessageFile.contentType, file.getSize(), timestamp);

		List<String> filename = List.of(file.getOriginalFilename().split("\\."));
		String text = filename.size() != 1 && IMAGE_EXTENSION.contains(filename.get(filename.size() - 1)) ? "(photo)" : "(file)";

		// update conversation latest message and timestamp
		chatService.updateConversationLatestMessage(sender, receiver, text, timestamp);

		OutgoingMessage outgoingMessage = new OutgoingMessage(incomingMessageFile.contentType, objectName, file.getSize(), System.currentTimeMillis(), sender, receiver);

		// Kafka Producer ⬇️
		String topic = KafkaConfig.MESSAGE_TOPIC;
		String message = new ObjectMapper().writeValueAsString(outgoingMessage);
		String key = String.format("%s.%s", sender, receiver);
		kafkaTemplate.send(topic, key, message);
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

	/**
	 * Handle read event.
	 */
	@MessageMapping("/event/read/{receiver}")
	public void sendReadEvent(@DestinationVariable String receiver, Map<String, Long> body, Principal principal) throws Exception {
		String sender = principal.getName();

		chatService.updateConversationRead(sender, receiver, body.get("timestamp"));
	}

}
