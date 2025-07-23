package com.example.app.entity;

import lombok.Data;

@Data
public class WxMessage {
    private String msgId;
    private String toUser;
    private String fromUser;
    private String content;
    private String createTime;
    private String msgType;
}
