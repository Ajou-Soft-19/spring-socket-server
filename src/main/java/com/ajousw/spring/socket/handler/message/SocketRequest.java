package com.ajousw.spring.socket.handler.message;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocketRequest {
    private RequestType requestType;
    //private String jwt;
    private Map<String, Object> data;
}
