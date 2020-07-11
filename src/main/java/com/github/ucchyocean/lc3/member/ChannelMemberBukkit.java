/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2020
 */
package com.github.ucchyocean.lc3.member;

import com.github.ucchyocean.lc3.LunaChatBukkit;
import com.github.ucchyocean.lc3.LunaChatConfig;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * チャンネルメンバーのBukkit抽象クラス
 * @author ucchy
 */
public abstract class ChannelMemberBukkit extends ChannelMember {

    /**
     * BukkitのPlayerを取得する
     * @return Player
     */
    public abstract Player getPlayer();

    /**
     * 発言者が今いる位置を取得する
     * @return 発言者の位置
     */
    public abstract Location getLocation();

    /**
     * 発言者が今いるワールドを取得する
     * @return 発言者の位置
     */
    public abstract World getWorld();

    /**
     * プレイヤーのグローバルチャンネルを取得する(Bukkitはサーバ名が取得できないため固定)
     * @return デフォルトのグローバルチャンネル名
     */
    @Override
    public String getGlobalChannelName() {
        LunaChatConfig config = LunaChatBukkit.getInstance().getLunaChatConfig();
        return config.getGlobalChannel(LunaChatConfig.DEFAULT_SERVER_NAME);
    }

    /**
     * プレイヤーのグローバルチャンネルを取得する(Bukkitはサーバ名が取得できないため固定)
     * @return デフォルトの強制参加チャンネル名リスト
     */
    @Override
    public List<String> getForceJoinChannels() {
        LunaChatConfig config = LunaChatBukkit.getInstance().getLunaChatConfig();
        return config.getForceJoinChannels(LunaChatConfig.DEFAULT_SERVER_NAME);
    }

    /**
     * CommandSenderから、ChannelMemberを作成して返す
     * @param sender
     * @return ChannelMember
     */
    public static ChannelMemberBukkit getChannelMemberBukkit(Object sender) {
        if ( sender == null || !(sender instanceof CommandSender) ) return null;
        if ( sender instanceof BlockCommandSender ) {
            return new ChannelMemberBlock((BlockCommandSender)sender);
        } else if ( sender instanceof ConsoleCommandSender ) {
            return new ChannelMemberBukkitConsole((ConsoleCommandSender)sender);
        } else {
            return ChannelMemberPlayer.getChannelPlayer((CommandSender)sender);
        }
    }
}
