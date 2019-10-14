/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.lc;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * プラグインのリソース管理クラス
 *
 * @author ucchy
 */
public class Resources {

    private static final String FILE_NAME = "messages.yml";

    private static YamlConfiguration defaultMessages;
    private static YamlConfiguration resources;

    /**
     * 初期化する
     */
    private static void initialize() {

        Path file = LunaChat.getInstance().getDataFolder().toPath().resolve(FILE_NAME);

        if (!Files.exists(file)) {
            LunaChat.getInstance().saveResource(FILE_NAME, false);
        }

        defaultMessages = loadDefaultMessages();
        resources = YamlConfiguration.loadConfiguration(file.toFile());
    }

    /**
     * リソースを取得する
     *
     * @param key リソースキー
     * @return リソース
     */
    public static String get(String key) {
        if (resources == null) initialize();
        String resource = resources.getString(key, defaultMessages.getString(key));
        if (resource == null) return defaultMessages.getString(key);
        return ChatColor.translateAlternateColorCodes('&', resource);
    }

    /**
     * Jarファイル内から直接 messages.yml を読み込み、YamlConfigurationにして返すメソッド
     *
     * @return YamlConfiguration
     */
    private static YamlConfiguration loadDefaultMessages() {
        YamlConfiguration messages = new YamlConfiguration();
        try {
            InputStream inputStream = LunaChat.getInstance().getResource(FILE_NAME);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(":") && !line.startsWith("#")) {
                        String key = line.substring(0, line.indexOf(":")).trim();
                        String value = line.substring(line.indexOf(":") + 1).trim();
                        if (value.startsWith("'") && value.endsWith("'"))
                            value = value.substring(1, value.length() - 1);
                        messages.set(key, value);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return messages;
    }
}
