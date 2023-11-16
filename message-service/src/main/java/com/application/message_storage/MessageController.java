package com.application.message_storage;

import com.application.config.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;


@RestController
@RequestMapping(path = "/message")
@Slf4j
public class MessageController {

	private final MessageService messageService;

	private final KafkaTemplate<String, String> kafkaTemplate;


	@Autowired
	public MessageController(MessageService messageService, KafkaTemplate<String, String> kafkaTemplate) {
		this.messageService = messageService;
		this.kafkaTemplate = kafkaTemplate;
	}


	/**
	 * Endpoint for sending text message.
	 */
	@PostMapping("/private-message/text")
	public void sendTextMessage(@RequestParam String receiver, @RequestBody IncomingMessage incomingMessage, Principal principal) throws Exception {
		log.info("✅ MessageController sendTextMessage {} -> {}", principal.getName(), receiver);

		if (incomingMessage.content.length() > 10000)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Text size too large");


		Long timestamp = System.currentTimeMillis();
		String sender = principal.getName();

		// save message to DynamoDB
		messageService.saveMessage(principal.getName(), receiver, incomingMessage.getContent(), "text", null, timestamp);

		// update conversation latest message and timestamp
		messageService.updateConversationLatestMessage(sender, receiver, incomingMessage.content, timestamp);

		OutgoingMessage outgoingMessage = new OutgoingMessage("text", incomingMessage.content, null, timestamp, sender, receiver);

		// Kafka Producer ⬇️
		String topic = KafkaConfig.MESSAGE_TOPIC;
		String message = new ObjectMapper().writeValueAsString(outgoingMessage);
		String key = String.format("%s.%s", sender, receiver);
		kafkaTemplate.send(topic, key, message);
	}

	@GetMapping("")
	public List<Message> getMessage(@RequestParam String receiver, @RequestParam Long timestamp, @RequestParam Integer pageSize, Principal principal){
		log.info("✅ MessageController getMessage {} -> {}", principal.getName(), receiver);
		String sender = principal.getName();

		return messageService.listMessage(sender, receiver, timestamp, pageSize);
	}

	/**
	 * Get temporary file download url.
	 */
	@GetMapping("/file/url")
	public String getFileUrl(@RequestParam String filename, @RequestParam String receiver, Principal principal) throws Exception {
		String sender = principal.getName();

		return messageService.getPresignedUrl(filename, receiver, sender);
	}


}
