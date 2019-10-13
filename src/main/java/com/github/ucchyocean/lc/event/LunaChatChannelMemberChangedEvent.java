/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.lc.event;

import java.util.List;

import com.github.ucchyocean.lc.channel.ChannelPlayer;

/**
 * メンバー変更イベント
 * @author ucchy
 */
public class LunaChatChannelMemberChangedEvent extends LunaChatBaseCancellableEvent {

    private final List<ChannelPlayer> before;
    private final List<ChannelPlayer> after;

    /**
     * コンストラクタ
     * @param channelName チャンネル名
     * @param before 変更前のメンバー
     * @param after 変更後のメンバー
     */
    public LunaChatChannelMemberChangedEvent(
            String channelName, List<ChannelPlayer> before, List<ChannelPlayer> after, boolean isAsync) {
        super(channelName, isAsync);
        this.before = before;
        this.after = after;
    }

    /**
     * 変更前のメンバーリストをかえす
     * @return 変更前のメンバーリスト
     */
    public List<ChannelPlayer> getMembersBefore() {
        return before;
    }

    /**
     * 変更後のメンバーリストをかえす
     * @return 変更後のメンバーリスト
     */
    public List<ChannelPlayer> getMembersAfter() {
        return after;
    }
}
