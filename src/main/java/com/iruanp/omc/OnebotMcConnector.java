package com.iruanp.omc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnebotMcConnector implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("onebot-mc-connector");
	private static OnebotWebSocketClient wsClient;
	private static Config config;
	private static MinecraftServer server;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Onebot-MC-Connector");
		
		// Load config
		config = Config.loadConfig();
		
		// Register server start/stop events
		ServerLifecycleEvents.SERVER_STARTING.register(mcServer -> {
			// Store server instance
			server = mcServer;
			// Initialize WebSocket client when server starts
			wsClient = new OnebotWebSocketClient(config);
			wsClient.connect();
		});

		// Server start notification
		ServerLifecycleEvents.SERVER_STARTED.register(mcServer -> {
			if (wsClient != null && wsClient.isConnected()) {
				wsClient.sendGroupMessage(config.groupNumber, 
					String.format(config.serverStartMessage, config.msg2qqPrefix));
			}
		});

		// Server stop notification
		ServerLifecycleEvents.SERVER_STOPPING.register(mcServer -> {
			if (wsClient != null && wsClient.isConnected()) {
				wsClient.sendGroupMessage(config.groupNumber, 
					String.format(config.serverStopMessage, config.msg2qqPrefix));
			}
			
			if (wsClient != null && wsClient.isOpen()) {
				wsClient.close();
			}
			server = null;
		});

		// Game message event
		ServerMessageEvents.GAME_MESSAGE.register((server, message, overlay) -> {
			if (wsClient != null && wsClient.isConnected()) {
				String msg = message.getString();
				// Skip messages that are our own broadcasts (starting with msg2mcPrefix)
				if (!msg.startsWith(String.format("[%s§f]", config.msg2mcPrefix))) {
					wsClient.sendGroupMessage(config.groupNumber, 
						String.format("[%s] %s", config.msg2qqPrefix, msg));
				}
			}
		});

		// Chat message event
		ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
			if (wsClient != null && wsClient.isConnected()) {
				// Format message with prefix
				String formattedMessage = String.format("[%s] <%s> %s", 
					config.msg2qqPrefix,
					sender.getName().getString(),
					message.getContent().getString()
				);
				
				// Send message to QQ group
				wsClient.sendGroupMessage(config.groupNumber, formattedMessage);
			}
		});

		LOGGER.info("Onebot-MC-Connector initialized");
	}

	public static void broadcastToMinecraft(String message) {
		if (message == null || message.isEmpty() || server == null) return;
		
		// Format message with prefix
		String formattedMessage = String.format("[%s§f] %s", config.msg2mcPrefix, message);
		
		// Broadcast message to all players
		server.getPlayerManager().broadcast(Text.of(formattedMessage), false);
	}
}