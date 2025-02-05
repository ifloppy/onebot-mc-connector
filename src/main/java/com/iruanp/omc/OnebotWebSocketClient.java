package com.iruanp.omc;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class OnebotWebSocketClient extends WebSocketClient {
    private final Config config;
    private final Gson gson;
    private boolean connected;

    public OnebotWebSocketClient(Config config) {
        super(URI.create(config.onebotUrl));
        this.config = config;
        this.gson = new Gson();
        this.connected = false;
        OnebotMcConnector.LOGGER.info("Initializing WebSocket client with URL: " + config.onebotUrl);

        // Add authorization header if token is provided
        if (config.onebotToken != null && !config.onebotToken.isEmpty()) {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + config.onebotToken);
            addHeader("Authorization", "Bearer " + config.onebotToken);
            OnebotMcConnector.LOGGER.info("Added authorization header");
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        OnebotMcConnector.LOGGER.info("Connected to OneBot WebSocket server with status: " + handshakedata.getHttpStatus());
        connected = true;
    }

    @Override
    public void onMessage(String message) {
        try {
            OnebotMcConnector.LOGGER.debug("Received message: " + message);
            JsonObject json = gson.fromJson(message, JsonObject.class);
            
            // Handle group messages
            if (json.has("post_type") && json.get("post_type").getAsString().equals("message")
                && json.has("message_type") && json.get("message_type").getAsString().equals("group")
                && json.has("group_id") && json.get("group_id").getAsString().equals(config.groupNumber)) {
                
                String sender = json.getAsJsonObject("sender").get("nickname").getAsString();
                JsonElement messageElement = json.get("message");
                String content = parseMessage(messageElement);
                
                if (content != null && !content.trim().isEmpty()) {
                    // Broadcast message to Minecraft
                    OnebotMcConnector.broadcastToMinecraft(String.format("<%s§f> %s", sender, content));
                }
            }
        } catch (Exception e) {
            OnebotMcConnector.LOGGER.error("Failed to handle message", e);
        }
    }

    private String parseMessage(JsonElement messageElement) {
        if (messageElement.isJsonPrimitive()) {
            return messageElement.getAsString();
        } else if (messageElement.isJsonArray()) {
            StringBuilder result = new StringBuilder();
            JsonArray msgArray = messageElement.getAsJsonArray();
            
            for (JsonElement element : msgArray) {
                if (element.isJsonObject()) {
                    JsonObject msgObj = element.getAsJsonObject();
                    String type = msgObj.get("type").getAsString();
                    
                    if ("text".equals(type)) {
                        result.append(msgObj.get("data").getAsJsonObject().get("text").getAsString());
                    } else if ("at".equals(type)) {
                        result.append("@").append(msgObj.get("data").getAsJsonObject().get("qq").getAsString());
                    } else if ("image".equals(type)) {
                        result.append("[图片]");
                    } else if ("face".equals(type)) {
                        result.append("[表情]");
                    }
                    // Add space between elements
                    result.append(" ");
                } else if (element.isJsonPrimitive()) {
                    result.append(element.getAsString()).append(" ");
                }
            }
            return result.toString().trim();
        }
        return null;
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        OnebotMcConnector.LOGGER.info("Disconnected from OneBot WebSocket server: " + reason + " (code: " + code + ", remote: " + remote + ")");
        connected = false;
        
        // Try to reconnect after 5 seconds
        if (remote) {
            try {
                Thread.sleep(5000);
                OnebotMcConnector.LOGGER.info("Attempting to reconnect...");
                this.reconnect();
            } catch (Exception e) {
                OnebotMcConnector.LOGGER.error("Failed to reconnect", e);
            }
        }
    }

    @Override
    public void onError(Exception ex) {
        OnebotMcConnector.LOGGER.error("WebSocket error", ex);
    }

    public boolean isConnected() {
        boolean isOpen = isOpen();
        OnebotMcConnector.LOGGER.debug("Connection status - connected: " + connected + ", isOpen: " + isOpen);
        return connected && isOpen;
    }

    public void sendGroupMessage(String groupId, String message) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("action", "send_group_msg");
            
            JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);
            params.addProperty("message", message);
            
            json.add("params", params);
            
            this.send(gson.toJson(json));
        } catch (Exception e) {
            OnebotMcConnector.LOGGER.error("Failed to send group message", e);
        }
    }
} 