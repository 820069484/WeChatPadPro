//package com.example.app.config;
//
//import cn.hutool.json.JSONObject;
//import cn.hutool.json.JSONUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.client.WebSocketClient;
//import org.springframework.web.socket.client.standard.StandardWebSocketClient;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//
//@Component
//@Slf4j
//public class WebSocketClientHandler extends TextWebSocketHandler {
//
//    private static final String WS_URL = "your_ws_url";
//    private final WebSocketClient webSocketClient;
//    private volatile WebSocketSession session;
//    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//    private final Object connectionLock = new Object();
//    private volatile boolean isReconnecting = false;
//
//    public WebSocketClientHandler() {
//        this.webSocketClient = new StandardWebSocketClient();
//        // 初始连接
//        connectWithRetry();
//        // 启动心跳任务
//        startHeartbeat();
//    }
//
//    /**
//     * 带重试机制的连接方法
//     */
//    private void connectWithRetry() {
//        synchronized (connectionLock) {
//            if (isReconnecting) return;
//            isReconnecting = true;
//        }
//
//        System.out.println("尝试连接WebSocket服务器...");
//
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//        executor.submit(() -> {
//            try {
//                // 关闭现有连接（如果存在）
//                if (session != null && session.isOpen()) {
//                    session.close();
//                }
//
//                // 建立新连接
//                session = webSocketClient.doHandshake(this, WS_URL).get();
//                System.out.println("WebSocket连接已建立");
//            } catch (Exception e) {
//                System.err.println("连接失败: " + e.getMessage());
//                e.printStackTrace();
//
//                // 延迟重试
//                scheduler.schedule(this::connectWithRetry, 5, TimeUnit.SECONDS);
//            } finally {
//                synchronized (connectionLock) {
//                    isReconnecting = false;
//                }
//                executor.shutdown();
//            }
//        });
//    }
//
//    /**
//     * 启动心跳机制
//     */
//    private void startHeartbeat() {
//        scheduler.scheduleAtFixedRate(() -> {
//            try {
//                if (isConnected()) {
//                    session.sendMessage(new TextMessage("{\"type\":\"heartbeat\"}"));
//                    System.out.println("❤️ 发送心跳");
//                } else {
//                    System.out.println("连接已断开，跳过心跳发送");
//                }
//            } catch (Exception e) {
//                System.err.println("心跳发送失败: " + e.getMessage());
//                // 不抛出异常，继续维持心跳任务
//            }
//        }, 0, 30, TimeUnit.SECONDS); // 每30秒发送一次心跳
//    }
//
//    /**
//     * 检查连接状态
//     */
//    private boolean isConnected() {
//        return session != null && session.isOpen();
//    }
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        System.out.println("✅ WebSocket连接已建立");
//        session.setTextMessageSizeLimit(1024 * 1024 * 10); // 设置文本消息大小限制为 1MB
//        session.setBinaryMessageSizeLimit(1024 * 1024 * 10); // 设置二进制消息大小限制为 1MB
//    }
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
//        try {
//            // 消息处理逻辑
//            JSONObject entries = JSONUtil.parseObj(message.getPayload());
//            String fromUser = entries.getJSONObject("from_user_name").getStr("str");
//            String toUser = entries.getJSONObject("to_user_name").getStr("str");
//            Integer msgType = entries.getInt("msg_type");
//            String content = entries.getJSONObject("content").getStr("str");
//            String createTimeStr = entries.getStr("create_time");
//        } catch (Exception e) {
//            log.error("处理消息时发生错误: {}", e.getMessage());
//        }
//    }
//
//    @Override
//    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
//        System.err.println("⚠️ WebSocket连接发生错误: " + exception.getMessage());
//        exception.printStackTrace();
//
//        // 错误处理但不关闭连接
//        if (session.isOpen()) {
//            System.out.println("连接仍然保持开启状态");
//        } else {
//            System.out.println("连接已断开，尝试重新连接...");
//            connectWithRetry();
//        }
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        System.out.println("🚫 WebSocket连接已关闭: " + status);
//        // 延迟后重新连接
//        scheduler.schedule(this::connectWithRetry, 5, TimeUnit.SECONDS);
//    }
//
//    /**
//     * 发送消息到服务器
//     */
//    public void sendMessage(String message) {
//        if (isConnected()) {
//            try {
//                session.sendMessage(new TextMessage(message));
//            } catch (Exception e) {
//                System.err.println("消息发送失败: " + e.getMessage());
//            }
//        } else {
//            System.out.println("无法发送消息，连接未建立");
//        }
//    }
//
//    /**
//     * 关闭WebSocket连接
//     */
//    public void closeConnection() {
//        if (session != null && session.isOpen()) {
//            try {
//                session.close(CloseStatus.NORMAL);
//                System.out.println("WebSocket连接已正常关闭");
//            } catch (Exception e) {
//                System.err.println("关闭WebSocket连接时发生错误: " + e.getMessage());
//            }
//        }
//        scheduler.shutdown(); // 关闭心跳任务
//    }
//}