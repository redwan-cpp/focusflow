package com.focusflow.model;

import java.time.LocalDateTime;

public class ChatMessage {
    public enum MessageType {
        CHAT, JOIN, LEAVE
    }
    private MessageType type;
    private Long roomId;
    private String sender;
    private String content;
    private LocalDateTime timestamp;

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
