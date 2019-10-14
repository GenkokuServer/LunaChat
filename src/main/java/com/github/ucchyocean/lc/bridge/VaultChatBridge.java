/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.lc.bridge;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Vault-Chat連携クラス
 *
 * @author ucchy
 */
public class VaultChatBridge {

    /**
     * vault-chatクラス
     */
    private Chat chatPlugin;

    /**
     * vault-chatをロードする
     *
     * @return ロードしたブリッジのインスタンス
     */
    public static VaultChatBridge load() {
        RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServicesManager().getRegistration(Chat.class);
        if (chatProvider != null) {
            VaultChatBridge bridge = new VaultChatBridge();
            bridge.chatPlugin = chatProvider.getProvider();
            return bridge;
        }
        return null;
    }

    /**
     * プレイヤーのprefixを取得します。
     *
     * @param player プレイヤー
     * @return プレイヤーのprefix
     */
    public String getPlayerPrefix(Player player) {
        return chatPlugin.getPlayerPrefix(player);
    }

    /**
     * プレイヤーのsuffixを取得します。
     *
     * @param player プレイヤー
     * @return プレイヤーのsuffix
     */
    public String getPlayerSuffix(Player player) {
        return chatPlugin.getPlayerSuffix(player);
    }
}
