package com.example.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)   // 忽略未知字段
public class WebhookPayload {
    private String msgId;
    private String fromUser;
    private String toUser;
    private String msgType;
    private Long timestamp;
    private String content;
    @JsonProperty("isSelfMsg")
    private Boolean selfMsg;
}