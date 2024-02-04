package com.ajousw.spring.socket.handler.message;

public enum MessageType {
    RESPONSE("RESPONSE"),
    ALERT("ALERT");

    private final String command;

    MessageType(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
