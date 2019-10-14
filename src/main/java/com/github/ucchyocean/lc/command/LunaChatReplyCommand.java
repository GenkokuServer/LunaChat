/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.lc.command;

import com.github.ucchyocean.lc.Resources;
import com.github.ucchyocean.lc.channel.ChannelPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * 1:1チャット受信コマンド
 *
 * @author ucchy
 */
public class LunaChatReplyCommand extends LunaChatMessageCommand {

    private static final String PREINFO = Resources.get("infoPrefix");
    private static final String PREERR = Resources.get("errorPrefix");

    /**
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // senderからChannelPlayerを作成する
        ChannelPlayer inviter = ChannelPlayer.getChannelPlayer(sender);

        // 会話相手を履歴から取得する
        String invitedName = DataMaps.privateMessageMap.get(inviter.getName());

        // 引数が無ければ、現在の会話相手を表示して終了する
        if (args.length == 0) {
            if (invitedName == null) sendResourceMessage(sender, PREINFO, "cmdmsgReplyInviterNone", inviter.getName());
            else sendResourceMessage(sender, PREINFO, "cmdmsgReplyInviter", inviter.getName(), invitedName);

            return true;
        }

        // 会話相手がからっぽなら、コマンドを終了する。
        if (invitedName == null) {
            sendResourceMessage(sender, PREERR, "errmsgNotfoundPM");
            return true;
        }

        // メッセージを取得する
        StringBuilder message = new StringBuilder();
        for (String arg : args) message.append(arg).append(" ");

        sendTellMessage(inviter, invitedName, message.toString().trim());
        return true;
    }
}
