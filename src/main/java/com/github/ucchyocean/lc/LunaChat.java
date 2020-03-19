/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.lc;

import com.github.ucchyocean.lc.bridge.DynmapBridge;
import com.github.ucchyocean.lc.bridge.MultiverseCoreBridge;
import com.github.ucchyocean.lc.bridge.VaultChatBridge;
import com.github.ucchyocean.lc.channel.ChannelManager;
import com.github.ucchyocean.lc.command.LunaChatCommand;
import com.github.ucchyocean.lc.command.LunaChatJapanizeCommand;
import com.github.ucchyocean.lc.command.LunaChatMessageCommand;
import com.github.ucchyocean.lc.command.LunaChatReplyCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * LunaChat プラグイン
 *
 * @author ucchy
 */
public class LunaChat extends JavaPlugin {

    private static LunaChat instance;

    private LunaChatConfig config;
    private LunaChatAPI manager;

    private VaultChatBridge vaultchat;
    private DynmapBridge dynmap;
    private MultiverseCoreBridge multiverse;

    private LunaChatLogger normalChatLogger;

    private LunaChatCommand lunachatCommand;
    private LunaChatMessageCommand messageCommand;
    private LunaChatReplyCommand replyCommand;
    private LunaChatJapanizeCommand lcjapanizeCommand;

    private PluginMessageChannelManager pluginMessageChannelManager;

    /**
     * プラグインが有効化されたときに呼び出されるメソッド
     *
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {
        // 変数などの初期化
        config = new LunaChatConfig();
        manager = new ChannelManager();
        normalChatLogger = new LunaChatLogger("==normalchat");
        pluginMessageChannelManager = new PluginMessageChannelManager();

        // チャンネルチャット無効なら、デフォルト発言先をクリアする(see issue #59)
        if (config.isDisableChannelChat()) manager.removeAllDefaultChannels();

        // Vault のロード
        Plugin temp = getServer().getPluginManager().getPlugin("Vault");
        if (temp != null) vaultchat = VaultChatBridge.load();


        // Dynmap のロード
        temp = getServer().getPluginManager().getPlugin("dynmap");
        if (temp != null) {
            dynmap = DynmapBridge.load(temp);
            if (dynmap != null) {
                getServer().getPluginManager().registerEvents(dynmap, this);
            }
        }

        // MultiverseCore のロード
        temp = getServer().getPluginManager().getPlugin("Multiverse-Core");
        if (temp != null) multiverse = MultiverseCoreBridge.load(temp);

        // リスナーの登録
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        // プラグインメッセージチャンネルに登録
        getServer().getMessenger().registerIncomingPluginChannel(this, "lunachat:out", new PluginMessageChannelManager());
        getServer().getMessenger().registerOutgoingPluginChannel(this, "lunachat:in");

        // コマンドの登録
        lunachatCommand = new LunaChatCommand();
        messageCommand = new LunaChatMessageCommand();
        replyCommand = new LunaChatReplyCommand();
        lcjapanizeCommand = new LunaChatJapanizeCommand();

        // 期限チェッカータスクの起動
        new ExpireCheckTask().runTaskTimerAsynchronously(this, 100, 1200);
    }

    /**
     * プラグインが無効化されたときに呼び出されるメソッド
     *
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
        Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        Bukkit.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    /**
     * コマンド実行時に呼び出されるメソッド
     *
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equals("lunachat")) {
            return lunachatCommand.onCommand(sender, command, label, args);
        } else if (command.getName().equals("tell")) {
            return messageCommand.onCommand(sender, command, label, args);
        } else if (command.getName().equals("reply")) {
            return replyCommand.onCommand(sender, command, label, args);
        } else if (command.getName().equals("lcjapanize")) {
            return lcjapanizeCommand.onCommand(sender, command, label, args);
        }

        return false;
    }

    /**
     * TABキー補完が実行されたときに呼び出されるメソッド
     *
     * @see org.bukkit.plugin.java.JavaPlugin#onTabComplete(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completeList = null;
        if (command.getName().equals("lunachat")) completeList = lunachatCommand.onTabComplete(sender, args);

        if (completeList != null) return completeList;

        return super.onTabComplete(sender, command, label, args);
    }

    /**
     * LunaChatのインスタンスを返す
     *
     * @return LunaChat
     */
    public static LunaChat getInstance() {
        if (instance == null) instance = (LunaChat) Bukkit.getPluginManager().getPlugin("LunaChat");
        return instance;
    }

    /**
     * LunaChatAPIを取得する
     *
     * @return LunaChatAPI
     */
    public LunaChatAPI getLunaChatAPI() {
        return manager;
    }

    /**
     * LunaChatConfigを取得する
     *
     * @return LunaChatConfig
     */
    public LunaChatConfig getLunaChatConfig() {
        return config;
    }

    /**
     * VaultChat連携クラスを返す
     *
     * @return VaultChatBridge
     */
    public VaultChatBridge getVaultChat() {
        return vaultchat;
    }

    /**
     * Dynmap連携クラスを返す
     *
     * @return DynmapBridge
     */
    public DynmapBridge getDynmap() {
        return dynmap;
    }

    /**
     * MultiverseCore連携クラスを返す
     *
     * @return MultiverseCoreBridge
     */
    public MultiverseCoreBridge getMultiverseCore() {
        return multiverse;
    }

    /**
     * 通常チャット用のロガーを返す
     *
     * @return normalChatLogger
     */
    public LunaChatLogger getNormalChatLogger() {
        return normalChatLogger;
    }

    /**
     * プラグインメッセージチャンネルマネージャを返す
     *
     * @return pluginMessageChannelManager
     */
    public PluginMessageChannelManager getPluginMessageChannelManager() {
        return pluginMessageChannelManager;
    }
}
