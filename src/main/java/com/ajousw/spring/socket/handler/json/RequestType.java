package com.ajousw.spring.socket.handler.json;

public enum RequestType {
    INIT("INIT"),
    UPDATE("UPDATE");

    private final String command;

    RequestType(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
