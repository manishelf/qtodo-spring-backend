package com.qtodo.socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer{	
	
    @Value("${qtodo.app.frontend.host.domain}")
    String frontendDomain;
    
//    @Autowired
//    WSJwtInterceptor wsJwtInterceptor;
    
    @Autowired
    WebSocketSessionManager wsManager;
    
//	@Override 
//	public void registerStompEndpoints(StompEndpointRegistry registry) {
//		registry.addEndpoint("/ws")
//        .setAllowedOrigins(frontendDomain); 
//        .withSockJS(); //needs sockjs client
//	}
//
//	@Override 
//	public void configureMessageBroker(MessageBrokerRegistry registry) {
//		registry.enableSimpleBroker("/topic");
//        registry.setApplicationDestinationPrefixes("/app/ws");
//	}

//	@Override
//	public void configureClientInboundChannel(ChannelRegistration registration) {
//		registration.interceptors(wsJwtInterceptor);
//	}
	
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SocketHandler(this.wsManager), "/ws").
                setAllowedOriginPatterns(frontendDomain);
    }
	
}
