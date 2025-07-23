package com.example.app.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.app.config.WebhookConfig;
import com.example.app.entity.WebhookPayload;
import com.example.app.entity.WxMessage;
import com.example.app.utils.ResultUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookConfig config;
    @RequestMapping(value = "/webhook", method = RequestMethod.HEAD)
    public ResponseEntity<Void> health() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/webhook")
    public Object webhook(@RequestBody byte[] rawBody,
                          @RequestHeader(value = "X-Webhook-Signature", required = false) String signature,
                          @RequestHeader(value = "X-Webhook-Timestamp", required = false) String timestamp) {

        // 1. 签名校验
        if (StrUtil.isNotBlank(config.getSecret())) {
            if (StrUtil.hasBlank(signature, timestamp)) {
                return ResultUtils.error(400, "Missing signature or timestamp");
            }
            if (!verifySignature(rawBody, signature, timestamp)) {
                log.warn("❌ Signature verification failed");
                return ResultUtils.error(403, "Invalid signature");
            }
        }
        try {
            // 2. 解析JSON
            JSONObject json = JSONUtil.parseObj(new String(rawBody, StandardCharsets.UTF_8));
            WebhookPayload payload = json.toBean(WebhookPayload.class);
            // 3. 格式化消息
            WxMessage wxMessage = formatMessage(payload);
            String toUser = wxMessage.getToUser();
            String fromUser = wxMessage.getFromUser();
            String content = wxMessage.getContent();
            String msgType = wxMessage.getMsgType();
            // 消息处理 这里写业务逻辑
            return ResultUtils.success("ok");
        } catch (Exception e) {
            log.error("❌ Error processing webhook", e);
            return ResultUtils.error(500, e.getMessage());
        }
    }

    /* ---------------- 私有工具 ---------------- */

    private boolean verifySignature(byte[] body, String sig, String ts) {
        HMac mac = new HMac(HmacAlgorithm.HmacSHA256, config.getSecret().getBytes(StandardCharsets.UTF_8));
        byte[] digest = mac.digest((ts + new String(body, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8));
        return HexUtil.encodeHexStr(digest).equalsIgnoreCase(sig);
    }

    private WxMessage formatMessage(WebhookPayload p) {
        /* --------------- 解析真正的文本内容 --------------- */
        String contentStr = "";
        Object contentObj = p.getContent();

        if (contentObj != null) {
            if (contentObj instanceof JSONObject) {
                // 当 content 本身是一个对象时（msgType=51 等场景）
                contentStr = ((JSONObject) contentObj).getStr("content", "");
            } else if (contentObj instanceof String) {
                // 当 content 就是纯文本或 XML 字符串时（msgType=1、49 等场景）
                String raw = (String) contentObj;
                if (raw.startsWith("{") && raw.endsWith("}")) {
                    // 偶尔出现字符串形态的 JSON，再解析一次
                    try {
                        JSONObject obj = JSONUtil.parseObj(raw);
                        contentStr = obj.getStr("content", raw);
                    } catch (Exception ignored) {
                        contentStr = raw;
                    }
                } else {
                    contentStr = raw;
                }
            } else {
                // 兜底
                contentStr = contentObj.toString();
            }
        }

        /* --------------- 格式化时间 --------------- */
        String timeStr;
        try {
            LocalDateTime ldt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(p.getTimestamp() * 1000L), ZoneId.systemDefault());
            timeStr = DateUtil.format(ldt, "yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            timeStr = String.valueOf(p.getTimestamp());
        }

        /* --------------- 组装日志 --------------- */
        LocalDateTime currentTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault());
        String sysTimeStr = DateUtil.format(currentTime, "yyyy-MM-dd HH:mm:ss");
        WxMessage wxMessage = new WxMessage();
        wxMessage.setMsgId(p.getMsgId());
        wxMessage.setToUser(p.getToUser());
        wxMessage.setFromUser(p.getFromUser());
        wxMessage.setContent(contentStr);
        wxMessage.setCreateTime(timeStr);
        wxMessage.setMsgType(p.getMsgType());

        String logStr = "\n✅ Received message: " +
                "🕒 MsgTime: " + timeStr + " " +
                "🆔 MsgID: " + p.getMsgId() + "\n" +
                "📨 Type: " + p.getMsgType() + " " +
                "👤 From: " + p.getFromUser() + " " +
                "🎯 To: " + p.getToUser() + "\n" +
                "💬 Content: " + contentStr + "\n" +
                "🕒 currentTime: " + sysTimeStr;
        log.info(logStr);

        return wxMessage;
    }
}