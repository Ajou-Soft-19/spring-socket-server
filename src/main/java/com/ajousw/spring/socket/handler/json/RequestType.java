package com.ajousw.spring.socket.handler.json;

public enum RequestType {
    INIT("INIT"),
    UPDATE("UPDATE"),
    CLOSE("CLOSE");

    private final String command;

    RequestType(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
