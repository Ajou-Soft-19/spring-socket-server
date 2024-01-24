package com.ajousw.spring.socket.handler.message;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocketRequest {
    private RequestType requestType;
    private String jwt;
    private Map<String, Object> data;
}
