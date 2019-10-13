/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.lc.event;

import com.github.ucchyocean.lc.LunaChat;
import com.github.ucchyocean.lc.channel.Channel;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 基底イベントクラス
 *
 * @author ucchy
 */
public abstract class LunaChatBaseEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    String channelName;

    /**
     * コンストラクタ
     *
     * @param channelName チャンネル名
     */
    LunaChatBaseEvent(String channelName, boolean isAsync) {
        super(isAsync);
        this.channelName = channelName;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * チャンネル名を取得する
     *
     * @return チャンネル名
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * チャンネルを取得する
     *
     * @return チャンネル
     */
    public Channel getChannel() {
        return LunaChat.getInstance().getLunaChatAPI().getChannel(channelName);
    }
}
