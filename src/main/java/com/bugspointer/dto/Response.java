package com.bugspointer.dto;

import lombok.Data;

@Data
public class Response
{
    private EnumStatus status;
    private Object content;
    private String message;

    public Response() {
    }

    public Response(EnumStatus status, Object content, String message) {
        this.status = status;
        this.content = content;
        this.message = message;
    }
}
