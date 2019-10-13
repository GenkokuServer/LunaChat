/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package com.github.ucchyocean.lc.channel;

import com.github.ucchyocean.lc.LunaChat;
import com.github.ucchyocean.lc.japanize.JapanizeType;

/**
 * Japanize2行表示のときに、変換結果を遅延してチャンネルに表示するためのタスク
 *
 * @author ucchy
 */
public class DelayedJapanizeChannelChatToBungeeTask extends DelayedJapanizeConvertTask {

    private final Channel channel;
    private final ChannelPlayer player;
    private final String lineFormat;

    /**
     * コンストラクタ
     *
     * @param org            変換前の文字列
     * @param type           変換タイプ
     * @param channel        変換後に発言する、発言先チャンネル
     * @param player         発言したプレイヤー
     * @param japanizeFormat 変換後に発言するときの、発言フォーマット
     * @param lineFormat     lineFormat
     */
    DelayedJapanizeChannelChatToBungeeTask(String org, JapanizeType type, Channel channel,
                                           ChannelPlayer player, String japanizeFormat, String lineFormat) {
        super(org, type, channel, player, japanizeFormat);
        this.channel = channel;
        this.player = player;
        this.lineFormat = lineFormat;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        if (runSync()) {
            String message = getResult();
            LunaChat.getInstance().getPluginMessageChannelManager()
                    .sendBungeeMessage(channel.getName(), player.getPlayer(), message, lineFormat);

        }
    }

}
