/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.lc.command;

import com.github.ucchyocean.lc.LunaChat;
import com.github.ucchyocean.lc.LunaChatAPI;
import com.github.ucchyocean.lc.LunaChatConfig;
import com.github.ucchyocean.lc.Resources;
import com.github.ucchyocean.lc.channel.Channel;
import com.github.ucchyocean.lc.channel.ChannelPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * サブコマンドの抽象クラス
 *
 * @author ucchy
 */
public abstract class SubCommandAbst {

    static final String PREINFO = Resources.get("infoPrefix");
    static final String PREERR = Resources.get("errorPrefix");
    final LunaChatAPI api;
    final LunaChatConfig config;

    /**
     * コンストラクタ
     */
    SubCommandAbst() {
        api = LunaChat.getInstance().getLunaChatAPI();
        config = LunaChat.getInstance().getLunaChatConfig();
    }

    /**
     * メッセージリソースのメッセージを、カラーコード置き換えしつつ、Channelに送信する
     *
     * @param channel メッセージの送り先
     * @param key     リソースキー
     * @param player  キーワード置き換えに使用するプレイヤー
     */
    void sendResourceMessageWithKeyword(Channel channel, String key, ChannelPlayer player) {
        String msg = Resources.get(key);
        if (msg == null || msg.equals("")) {
            return;
        }

        msg = msg.replace("%ch", channel.getName());
        msg = msg.replace("%color", channel.getColorCode());

        if (player != null) {
            msg = msg.replace("%username", player.getDisplayName());
            msg = msg.replace("%player", player.getName());
        } else {
            msg = msg.replace("%username", "");
            msg = msg.replace("%player", "");
        }
        msg = ChatColor.translateAlternateColorCodes('&', msg);
        channel.sendMessage(null, msg, null, true, "system", false);
    }

    /**
     * メッセージリソースのメッセージを、カラーコード置き換えしつつ、Channelに送信する
     *
     * @param channel メッセージの送り先
     * @param key     リソースキー
     * @param player  キーワード置き換えに使用するプレイヤー
     * @param minutes キーワード置き換えに使用する数値
     */
    void sendResourceMessageWithKeyword(Channel channel, String key, ChannelPlayer player, int minutes) {
        String msg = Resources.get(key);
        if (msg == null || msg.equals("")) return;

        msg = msg.replace("%ch", channel.getName());
        msg = msg.replace("%color", channel.getColorCode());
        msg = msg.replace("%d", String.valueOf(minutes));

        if (player != null) {
            msg = msg.replace("%username", player.getDisplayName());
            msg = msg.replace("%player", player.getName());
        } else {
            msg = msg.replace("%username", "");
            msg = msg.replace("%player", "");
        }

        msg = ChatColor.translateAlternateColorCodes('&', msg);
        channel.sendMessage(null, msg, null, true, "system", false);
    }

    /**
     * メッセージリソースのメッセージを、カラーコード置き換えしつつ、senderに送信する
     *
     * @param sender メッセージの送り先
     * @param pre    プレフィックス
     * @param key    リソースキー
     * @param args   リソース内の置き換え対象キーワード
     */
    void sendResourceMessage(CommandSender sender, String pre, String key, Object... args) {
        String org = Resources.get(key);
        if (org == null || org.equals("")) return;
        sender.sendMessage(String.format(pre + org, args));
    }

    /**
     * メッセージリソースのメッセージを、カラーコード置き換えしつつ、ChannelPlayerに送信する
     *
     * @param key  リソースキー
     * @param args リソース内の置き換え対象キーワード
     */
    void sendResourceMessage(ChannelPlayer cp, String key, Object... args) {
        String org = Resources.get(key);
        if (org == null || org.equals("")) return;
        cp.sendMessage(String.format(SubCommandAbst.PREINFO + org, args));
    }

    /**
     * コマンドを取得します。
     *
     * @return コマンド
     */
    public abstract String getCommandName();

    /**
     * パーミッションノードを取得します。
     *
     * @return パーミッションノード
     */
    public abstract String getPermissionNode();

    /**
     * コマンドの種別を取得します。
     *
     * @return コマンド種別
     */
    protected abstract CommandType getCommandType();

    /**
     * 使用方法に関するメッセージをsenderに送信します。
     *
     * @param sender コマンド実行者
     * @param label  実行ラベル
     */
    protected abstract void sendUsageMessage(CommandSender sender, String label);

    /**
     * コマンドを実行します。
     *
     * @param sender コマンド実行者
     * @param label  実行ラベル
     * @param args   実行時の引数
     * @return コマンドが実行されたかどうか
     */
    public abstract boolean runCommand(CommandSender sender, String label, String[] args);

    /**
     * コマンドの種別
     *
     * @author ucchy
     */
    protected enum CommandType {

        /**
         * 一般ユーザー向けコマンド
         */
        USER,

        /**
         * チャンネルモデレーター向けコマンド
         */
        MODERATOR,

        /**
         * サーバー管理者向けコマンド
         */
        ADMIN
    }
}
