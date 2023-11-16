package com.application.file_storage;

import com.application.config.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;


@RestController
@RequestMapping(path = "/filestorage")
@Slf4j
public class FileStorageController {

	private final FileStorageService fileStorageService;

	private final KafkaTemplate<String, String> kafkaTemplate;

	private static final List<String> IMAGE_EXTENSION = List.of("jpeg", "jpg", "gif", "png");

	@Autowired
	public FileStorageController(FileStorageService messageService, KafkaTemplate<String, String> kafkaTemplate) {
		this.fileStorageService = messageService;
		this.kafkaTemplate = kafkaTemplate;
	}
	/**
	 * Endpoint for sending file message.
	 */
	@PostMapping("/private-message/file")
	public void sendFileMessage(@RequestParam String receiver, IncomingMessageFile incomingMessageFile, Principal principal) throws Exception {
		log.info("✅ FileStorageController sendFileMessage {} -> {}", principal.getName(), receiver);

		if(incomingMessageFile.file.getSize() > 100000000.0)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size larger than 100MB not supported");

		// set the file content
		MultipartFile file = incomingMessageFile.file;
		String sender = principal.getName();

		// TODO: if an image, read height & width and store in DynamoDB

		// S3 upload
		String objectName = fileStorageService.saveFile(sender, receiver, file);

		// set timestamp after file upload
		Long timestamp = System.currentTimeMillis();

		// save message
		fileStorageService.saveMessage(sender, receiver, objectName, "file", file.getSize(), timestamp);

		List<String> filename = List.of(file.getOriginalFilename().split("\\."));
		String text = filename.size() != 1 && IMAGE_EXTENSION.contains(filename.get(filename.size() - 1)) ? "(photo)" : "(file)";

		// update conversation latest message and timestamp
		fileStorageService.updateConversationLatestMessage(sender, receiver, text, timestamp);

		OutgoingMessage outgoingMessage = new OutgoingMessage("file", objectName, file.getSize(), System.currentTimeMillis(), sender, receiver);

		// Kafka Producer ⬇️
		String topic = KafkaConfig.MESSAGE_TOPIC;
		String message = new ObjectMapper().writeValueAsString(outgoingMessage);
		String key = String.format("%s.%s", sender, receiver);
		kafkaTemplate.send(topic, key, message);
	}

	/**
	 * Download file from S3
	 */
//	@GetMapping("/message/file")
	public byte[] getFile(@RequestParam String filename, @RequestParam String receiver, Principal principal, HttpServletResponse response) throws Exception {
		String sender = principal.getName();

		response.setHeader("Content-Disposition", "attachment; filename="+ filename);
		return fileStorageService.getFile(filename, receiver, sender);
	}
}
