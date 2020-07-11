/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2020
 */
package com.github.ucchyocean.lc3.member;

import com.github.ucchyocean.lc3.LunaChatBungee;
import com.github.ucchyocean.lc3.LunaChatConfig;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.util.List;

/**
 * チャンネルメンバーのBungee抽象クラス
 * @author ucchy
 */
public abstract class ChannelMemberBungee extends ChannelMember {

    /**
     * BungeeのProxiedPlayerを取得する
     * @return ProxiedPlayer
     */
    public abstract ProxiedPlayer getPlayer();

    /**
     * 発言者が今いるサーバーを取得する
     * @return サーバー
     */
    public abstract Server getServer();


    /**
     * プレイヤーが参加しているサーバのグローバルチャンネル名を返す
     * @return グローバルチャンネル名
     */
    @Override
    public String getGlobalChannelName() {
        LunaChatConfig config = LunaChatBungee.getInstance().getConfig();
        String serverName = null;

        Server server = this.getServer();
        if (server != null) {
            serverName = server.getInfo().getName();
        }
        return config.getGlobalChannel(serverName);
    }

    /**
     * プレイヤーの強制参加チャンネル名を取得する
     * @return 強制参加チャンネル名リスト
     */
    @Override
    public List<String> getForceJoinChannels() {
        LunaChatConfig config = LunaChatBungee.getInstance().getConfig();
        String serverName = null;

        Server server = this.getServer();
        if (server != null) {
            serverName = server.getInfo().getName();
        }
        return config.getForceJoinChannels(serverName);
    }

    /**
     * CommandSenderから、ChannelMemberを作成して返す
     * @param sender
     * @return ChannelMember
     */
    public static ChannelMemberBungee getChannelMemberBungee(Object sender) {
        if ( sender == null || !(sender instanceof CommandSender) ) return null;
        if ( sender instanceof ProxiedPlayer ) {
            return new ChannelMemberProxiedPlayer(((ProxiedPlayer)sender).getUniqueId());
        } else {
            // ProxiedPlayer以外のCommandSenderは、ConsoleSenderしかないはず
            return new ChannelMemberBungeeConsole((CommandSender)sender);
        }
    }
}
