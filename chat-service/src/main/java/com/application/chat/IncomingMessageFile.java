package com.application.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;


@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class IncomingMessageFile {

	public String contentType;
	public MultipartFile file;

}
