package com.qtodo.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.google.protobuf.InvalidProtocolBufferException;
import com.qtodo.auth.CustomUserDetails;
import com.qtodo.auth.UserPermission;
import com.qtodo.response.ValidationException;
import com.qtodo.socket.proto.SockMessage.MessageProto;

public class SocketHandler extends BinaryWebSocketHandler {
	
	WebSocketSessionManager wsManager;
	
	public SocketHandler(WebSocketSessionManager wsManager) {
		this.wsManager = wsManager;
	}

	@Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException { 
		
		var user = getUserFromSession(session);

        this.wsManager.addWebSocketToUserGroupSession(user.getUserGroup(), session);
        MessageProto connectionMessage = MessageProto.newBuilder()
                .setType(MessageTypes.CONNECTED.toString())
                .setMessage("Connected to "+user.getUserGroup() + " group session "
                		+ this.wsManager.getWebSocketInGroupSessionsExcept(user.getUserGroup(), session).size()
                		+" Others are online")
                .setCreatedAt(getNowAsProto())
                .build();

        byte[] payload = connectionMessage.toByteArray();

        session.sendMessage(new BinaryMessage(payload));
        
        System.out.println("connected to "+session.getId() + user.getUserEmail());

    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        super.handleMessage(session, message);
        if(message instanceof BinaryMessage) {
        	try {
        		ByteBuffer payloadBuffer = ((BinaryMessage)message).getPayload();    
    	        byte[] binaryPayload = new byte[payloadBuffer.remaining()];
    	        payloadBuffer.get(binaryPayload); 
                MessageProto incomingMessage = MessageProto.parseFrom(binaryPayload);
                System.out.println(incomingMessage.toString());
                switch(MessageTypes.valueOf(incomingMessage.getType())) {
		            case REFRESH_MERGE: 	
		            	sendRefreshToMembers(session);
		            	break;
                }
                
            } catch (InvalidProtocolBufferException e) {
            	throw ValidationException.failedFor("Socket message derserialization", e.getMessage());
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    	this.wsManager.removeWebSocketSession(getUserFromSession(session).getUserGroup(), session);
        super.afterConnectionClosed(session, status);
        System.out.println("DISCONNECTED " + session.getId() + 
                           " | Reason: " + status.getReason() + 
                           " | Code: " + status.getCode());
        System.out.println("disconnected "+session.getId());
    }
    
    private CustomUserDetails getUserFromSession(WebSocketSession session) {
    	return (CustomUserDetails) ((UsernamePasswordAuthenticationToken)session.getPrincipal()).getPrincipal();
    }
    
    private com.google.protobuf.Timestamp getNowAsProto(){
        Instant now = Instant.now();
        com.google.protobuf.Timestamp created = com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();
        return created;
    }

    private void sendRefreshToMembers(WebSocketSession session) {
    	var user = getUserFromSession(session);
    	var authorities = user.getAuthorities();
    	var hasPermission = false;
    	
        for(int i = 0 ; i< authorities.size(); i++){
        	var a = authorities.get(i);
    		hasPermission = hasPermission
    				|| a.getAuthority().equals(UserPermission.WRITE.toString()) 
    				|| a.getAuthority().equals(UserPermission.EDIT.toString()); 
        }
        
        if(!hasPermission) return;
        
    	var outgoing = MessageProto.newBuilder()
					.setType(MessageTypes.REFRESH_MERGE.toString())
					.setCreatedAt(getNowAsProto())
					.build();
		var payload = new BinaryMessage(outgoing.toByteArray());
		
		System.out.println("Sending sync to group");
		this.wsManager.getWebSocketInGroupSessionsExcept(user.getUserGroup(), session)
			.forEach(x->{
				try {
					System.out.println(x.getId());
					x.sendMessage(payload);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
    }
}	