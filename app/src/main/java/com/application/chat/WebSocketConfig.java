package com.application.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

// integrate spring session into websocket
//import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.session.web.socket.config.annotation.AbstractSessionWebSocketMessageBrokerConfigurer;
import org.springframework.session.Session;

// simple websocket conifguration

//@Configuration
//@EnableWebSocketMessageBroker
//public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
//
//	@Override
//	public void configureMessageBroker(MessageBrokerRegistry config) {
//		config.enableSimpleBroker("/topic", "/user");
//		// /topic: hello message
//		// /user: private message
//		config.setApplicationDestinationPrefixes("/app");
//		config.setUserDestinationPrefix("/user");
//	}
//
//	@Override
//	public void registerStompEndpoints(StompEndpointRegistry registry) {
//		registry.addEndpoint("/gs-guide-websocket").withSockJS();
//	}
//
//}

// spring session and websocket integration
// ref: https://docs.spring.io/spring-session/reference/guides/boot-websocket.html

// good explanation of each configuration step:
// ref: https://manhtai.github.io/posts/spring-websocket-server/
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractSessionWebSocketMessageBrokerConfigurer<Session>  {

	@Value("${frontend.url}")
	private String frontendUrl;

	@Value("${rabbitmq.host}")
	private String rabbitmqHost;

	@Value("${rabbitmq.port}")
	private int rabbitmqPort;

	@Value("${rabbitmq.username}")
	private String rabbitmqUsername;

	@Value("${rabbitmq.password}")
	private String rabbitmqPassword;

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		// Note: don't add "/user" into simple broker
		config.enableStompBrokerRelay("/topic", "/queue")
				.setUserDestinationBroadcast("/topic/log-unresolved-user")
				.setUserRegistryBroadcast("/topic/log-user-registry")
				.setRelayHost(rabbitmqHost)
				.setRelayPort(rabbitmqPort)
				.setClientLogin(rabbitmqUsername)
				.setClientPasscode(rabbitmqPassword);

		config.setApplicationDestinationPrefixes("/app");

		// prefix of endpoint to receive "private" message (see setUserDestinationPrefix Documentation)
		config.setUserDestinationPrefix("/user");
	}

	@Override
	protected void configureStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/gs-guide-websocket").setAllowedOrigins(frontendUrl).withSockJS();
	}

}
