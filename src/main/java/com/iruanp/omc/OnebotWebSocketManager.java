package com.iruanp.omc;

public class OnebotWebSocketManager {
    private final Config config;
    private volatile boolean running = true;
    //private static final int RECONNECT_DELAY_MS = 5000;
    private volatile OnebotWebSocketClient currentClient;

    public OnebotWebSocketManager(Config config) {
        this.config = config;
    }

    public void start() {
        Thread connectionThread = new Thread(this::maintainConnection, "WebSocket-Connection-Thread");
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    private void maintainConnection() {
        while (running) {
            currentClient = null;
            try {
                currentClient = new OnebotWebSocketClient(config);
                currentClient.connectBlocking();

                // 等待直到连接断开
                while (currentClient.isConnected() && running) {
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                OnebotMcConnector.LOGGER.error("WebSocket connection error", e);
            } finally {
                if (currentClient != null) {
                    try {
                        currentClient.close();
                    } catch (Exception e) {
                        OnebotMcConnector.LOGGER.error("Error closing WebSocket client", e);
                    }
                }

                // if (running) {
                //     try {
                //         OnebotMcConnector.LOGGER.info("Waiting " + RECONNECT_DELAY_MS + "ms before reconnecting...");
                //         Thread.sleep(RECONNECT_DELAY_MS);
                //     } catch (InterruptedException ie) {
                //         Thread.currentThread().interrupt();
                //         break;
                //     }
                // }
            }
        }
    }

    public void stop() {
        running = false;
    }

    public boolean isConnected() {
        return currentClient != null && currentClient.isConnected();
    }

    public void sendGroupMessage(String message) {
        if (isConnected()) {
            currentClient.sendGroupMessage(config.groupNumber, message);
        } else {
            OnebotMcConnector.LOGGER.warn("WebSocket client is not connected, message not sent");
        }
    }
}