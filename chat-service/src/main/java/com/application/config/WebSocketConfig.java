package com.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompReactorNettyCodec;
import org.springframework.messaging.tcp.reactor.ReactorNettyTcpClient;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

// integrate spring session into websocket
//import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.session.web.socket.config.annotation.AbstractSessionWebSocketMessageBrokerConfigurer;
import org.springframework.session.Session;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.ArrayList;
import java.util.Arrays;

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

//	@Value("${spring.profiles.active}")
//	private String profile;

	@Value("${frontend.url}")
	private String frontendUrl;

//	@Value("${rabbitmq.host}")
//	private String rabbitmqHost;
//
//	@Value("${rabbitmq.port}")
//	private int rabbitmqPort;

//	@Value("${rabbitmq.username}")
//	private String rabbitmqUsername;
//
//	@Value("${rabbitmq.password}")
//	private String rabbitmqPassword;

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {

		// in-memory broker: https://docs.spring.io/spring-framework/reference/web/websocket/stomp/handle-simple-broker.html
		config.enableSimpleBroker("/topic", "/queue");

		// external broker(rabbitmq): https://docs.spring.io/spring-framework/reference/web/websocket/stomp/handle-broker-relay.html

//		ArrayList<String> LocalProfileList = new ArrayList<String>(Arrays.asList("Local"));
//
//		if (LocalProfileList.contains(profile)) {
//			// localhost MQ
//			config.enableStompBrokerRelay("/topic", "/queue")
//					.setAutoStartup(true)	// not sure (?)
//					.setUserDestinationBroadcast("/topic/log-unresolved-user")
//					.setUserRegistryBroadcast("/topic/log-user-registry")
//					.setRelayHost(rabbitmqHost)
//					.setRelayPort(rabbitmqPort)
//					.setSystemLogin(rabbitmqUsername)
//					.setSystemPasscode(rabbitmqPassword)
//					.setClientLogin(rabbitmqUsername)
//					.setClientPasscode(rabbitmqPassword);
//		}
//		else {
//			// AWS MQ
//			// AWS MQ need TCP Client
//			ReactorNettyTcpClient<byte[]> tcpClient = new ReactorNettyTcpClient<>(builder ->
//					builder
//							.host(rabbitmqHost)
//							.port(rabbitmqPort)
//							.secure()
//					, new StompReactorNettyCodec());
//
//			// Note: don't add "/user" into simple broker
//			config.enableStompBrokerRelay("/topic", "/queue")
//					.setAutoStartup(true)    // not sure (?)
//					.setUserDestinationBroadcast("/topic/log-unresolved-user")
//					.setUserRegistryBroadcast("/topic/log-user-registry")
////				.setRelayHost(rabbitmqHost)
////				.setRelayPort(rabbitmqPort)
//					.setSystemLogin(rabbitmqUsername)
//					.setSystemPasscode(rabbitmqPassword)
//					.setClientLogin(rabbitmqUsername)
//					.setClientPasscode(rabbitmqPassword)
//					// need both System and Client credentials
//					.setTcpClient(tcpClient);
//		}


		config.setApplicationDestinationPrefixes("/app");

		// prefix of endpoint to receive "private" message (see setUserDestinationPrefix Documentation)
		config.setUserDestinationPrefix("/user");
	}

	@Override
	protected void configureStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/chat/websocket").setAllowedOrigins(frontendUrl);
	}

	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
		registration.setMessageSizeLimit(1024 * 1024 * 1024);
		registration.setSendTimeLimit(15 * 1000).setSendBufferSizeLimit(512 * 1024);
	}

}