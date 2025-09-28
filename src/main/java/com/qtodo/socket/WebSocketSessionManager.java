package com.qtodo.socket;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class WebSocketSessionManager {

	private final Map<String, Set<WebSocketSession>> webSocketSessionsPerGroup = new HashMap<>();

	public Set<WebSocketSession> getWebSocketInGroupSessionsExcept(String userGroup, WebSocketSession webSocketSession) {
		System.out.println("get"+userGroup);
		var nonMatchingSessions = this.webSocketSessionsPerGroup.get(userGroup).stream()
				.filter(x -> !x.getId().equalsIgnoreCase(webSocketSession.getId())).collect(Collectors.toSet());
		return nonMatchingSessions;
	}

	public void addWebSocketToUserGroupSession(String userGroup, WebSocketSession webSocketSession) {
		System.out.println("add"+userGroup);
		var ugSet = webSocketSessionsPerGroup.get(userGroup);
		if(ugSet == null) {
			ugSet = new HashSet<WebSocketSession>();
		}
		ugSet.add(webSocketSession);
		this.webSocketSessionsPerGroup.put(userGroup, ugSet);
	}

	public void removeWebSocketSession(String userGroup, WebSocketSession webSocketSession) {
		System.out.println("del"+userGroup);
		var matchingSessions = this.webSocketSessionsPerGroup.get(userGroup).stream()
				.filter(x -> x.getId().equalsIgnoreCase(webSocketSession.getId())).collect(Collectors.toSet());

		this.webSocketSessionsPerGroup.get(userGroup).removeAll(matchingSessions);
	}

}
