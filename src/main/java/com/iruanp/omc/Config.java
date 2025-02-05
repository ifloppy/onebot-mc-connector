package com.iruanp.omc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    public String groupNumber;
    public String onebotUrl;
    public String onebotToken;
    public String msg2qqPrefix;
    public String msg2mcPrefix;
    public String serverStartMessage;
    public String serverStopMessage;

    public static Config loadConfig() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Path configPath = Path.of("config/onebot-mc-connector.json");

            // Create default config
            Config defaultConfig = new Config();
            defaultConfig.groupNumber = "123456789";
            defaultConfig.onebotUrl = "ws://127.0.0.1:6700";
            defaultConfig.onebotToken = "";
            defaultConfig.msg2qqPrefix = "MC";
            defaultConfig.msg2mcPrefix = "QQ";
            defaultConfig.serverStartMessage = "[%s] Server has started!";
            defaultConfig.serverStopMessage = "[%s] Server is stopping...";

            // Create config file if it doesn't exist
            if (!Files.exists(configPath)) {
                try {
                    Files.createDirectories(configPath.getParent());
                    Files.writeString(configPath, gson.toJson(defaultConfig));
                    OnebotMcConnector.LOGGER.info("Created default config file");
                    return defaultConfig;
                } catch (IOException e) {
                    OnebotMcConnector.LOGGER.error("Failed to create default config", e);
                    return defaultConfig;
                }
            }

            // Load existing config
            try {
                Config config = gson.fromJson(Files.readString(configPath), Config.class);
                
                // Validate config values and use defaults if any are null
                if (config.groupNumber == null) config.groupNumber = defaultConfig.groupNumber;
                if (config.onebotUrl == null) config.onebotUrl = defaultConfig.onebotUrl;
                if (config.onebotToken == null) config.onebotToken = defaultConfig.onebotToken;
                if (config.msg2qqPrefix == null) config.msg2qqPrefix = defaultConfig.msg2qqPrefix;
                if (config.msg2mcPrefix == null) config.msg2mcPrefix = defaultConfig.msg2mcPrefix;
                if (config.serverStartMessage == null) config.serverStartMessage = defaultConfig.serverStartMessage;
                if (config.serverStopMessage == null) config.serverStopMessage = defaultConfig.serverStopMessage;
                
                return config;
            } catch (IOException e) {
                OnebotMcConnector.LOGGER.error("Failed to load config, using default", e);
                return defaultConfig;
            }
        } catch (Exception e) {
            OnebotMcConnector.LOGGER.error("Unexpected error in config loading", e);
            return new Config();
        }
    }
} 