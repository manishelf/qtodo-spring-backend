package com.qtodo.socket;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.qtodo.auth.CustomUserDetails;

@Component
public class WebSocketSessionManager {

	private final Map<String, Set<WebSocketSession>> webSocketSessionsPerGroup = new HashMap<>();

	public Set<WebSocketSession> getWebSocketInGroupSessionsExcept(String userGroup, WebSocketSession webSocketSession) {
		var nonMatchingSessions = this.webSocketSessionsPerGroup.get(userGroup).stream()
				.filter(x -> !x.getId().equalsIgnoreCase(webSocketSession.getId())).collect(Collectors.toSet());
		return nonMatchingSessions;
	}
	
	public Optional<WebSocketSession> getWebSocketInGroupSession(String userGroup, String email){
		var connections = this.webSocketSessionsPerGroup.get(userGroup);
		
		if(connections == null) return Optional.empty();
		
		var matchingSession = connections.stream()
				.filter(x-> {
					var userDetails = (CustomUserDetails) ((UsernamePasswordAuthenticationToken)x.getPrincipal()).getPrincipal();
					return userDetails.getUserEmail().equals(email);
				}).findFirst();
		return matchingSession;
	}

	public void addWebSocketToUserGroupSession(String userGroup, WebSocketSession webSocketSession) {
		var ugSet = webSocketSessionsPerGroup.get(userGroup);
		if(ugSet == null) {
			ugSet = new HashSet<WebSocketSession>();
		}
		ugSet.add(webSocketSession);
		this.webSocketSessionsPerGroup.put(userGroup, ugSet);
	}

	public void removeWebSocketSession(String userGroup, WebSocketSession webSocketSession) {
		var matchingSessions = this.webSocketSessionsPerGroup.get(userGroup).stream()
				.filter(x -> x.getId().equalsIgnoreCase(webSocketSession.getId())).collect(Collectors.toSet());

		this.webSocketSessionsPerGroup.get(userGroup).removeAll(matchingSessions);
	}

}
