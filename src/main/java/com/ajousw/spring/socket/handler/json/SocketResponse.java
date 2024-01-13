package com.ajousw.spring.socket.handler.json;

import com.ajousw.spring.web.controller.json.ResponseStatusCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Getter
@ToString
@NoArgsConstructor
public class SocketResponse {
    public int code;
    public Object data;

    public SocketResponse(int code, Object data) {
        this.code = code;
        this.data = data;
    }

    public SocketResponse(Object data) {
        this.code = ResponseStatusCode.OK;
        this.data = data;
    }
}



