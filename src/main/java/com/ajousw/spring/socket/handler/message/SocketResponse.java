package com.ajousw.spring.socket.handler.message;

import com.ajousw.spring.web.controller.json.ResponseStatusCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Getter
@ToString
@NoArgsConstructor
public class SocketResponse {
    public int code;
    public MessageType messageType;
    public Object data;

    public SocketResponse(int code, Object data) {
        this.code = code;
        this.messageType = MessageType.RESPONSE;
        this.data = data;
    }

    public SocketResponse(Object data) {
        this.code = ResponseStatusCode.OK;
        this.messageType = MessageType.RESPONSE;
        this.data = data;
    }

    public SocketResponse(MessageType messageType, Object data) {
        this.code = ResponseStatusCode.OK;
        this.messageType = messageType;
        this.data = data;
    }
}



