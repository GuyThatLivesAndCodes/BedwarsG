package com.guythatlives.bedwarsg;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final BedwarsG plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private Map<String, FileConfiguration> configs;

    public ConfigManager(BedwarsG plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
    }

    public void loadConfigs() {
        // Create plugin folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        // Load main config
        plugin.saveDefaultConfig();
        config = plugin.getConfig();

        // Load messages config
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        // Create maps, arenas, and stats folders
        createFolder("maps");
        createFolder("arenas");
        createFolder("stats");

        plugin.getLogger().info("Configuration files loaded successfully!");
    }

    private void createFolder(String folderName) {
        File folder = new File(plugin.getDataFolder(), folderName);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();

        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        plugin.getLogger().info("Configuration files reloaded!");
    }

    public void saveConfig(String name, FileConfiguration config) {
        try {
            File file = new File(plugin.getDataFolder(), name + ".yml");
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save " + name + ".yml!");
            e.printStackTrace();
        }
    }

    public FileConfiguration loadConfig(String name) {
        File file = new File(plugin.getDataFolder(), name + ".yml");
        if (!file.exists()) {
            return null;
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public String getMessage(String path) {
        String message = messages.getString(path);
        if (message == null) {
            return ChatColor.RED + "Message not found: " + path;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getMessage(String path, Map<String, String> placeholders) {
        String message = getMessage(path);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }

    public String getPrefix() {
        return getMessage("prefix");
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public double getDouble(String path) {
        return config.getDouble(path);
    }
}
