package com.application;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

// integrate spring session into websocket
//import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.session.web.socket.config.annotation.AbstractSessionWebSocketMessageBrokerConfigurer;
import org.springframework.session.Session;

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

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractSessionWebSocketMessageBrokerConfigurer<Session>  {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic", "/user");
		// /topic: hello message
		// /user: private message
		config.setApplicationDestinationPrefixes("/app");
		config.setUserDestinationPrefix("/user");
	}

	@Override
	protected void configureStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/gs-guide-websocket").withSockJS();
	}

}
