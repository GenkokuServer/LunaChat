package com.github.ucchyocean.lc;

import com.github.ucchyocean.lc.channel.Channel;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public class PluginMessageChannelManager implements PluginMessageListener {

    private static final LunaChat lunaChat = LunaChat.getInstance();

    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, @NotNull byte[] message) {
        if (!channel.equalsIgnoreCase("lunachat:out")) return;

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));

        try {
            Channel lunaChatChannel = lunaChat.getLunaChatAPI().getChannel(in.readUTF());
            if (lunaChatChannel == null) return;

            String sender = in.readUTF();
            String chat = in.readUTF();
            String lineFormat = in.readUTF();
            String serverName = in.readUTF();

            if (!lineFormat.equals("")) {
                lunaChatChannel.chatFromOtherSource(sender, serverName, chat, false);
            } else {
                lunaChatChannel.sendMessage(null, chat, null, false, sender, true);
            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendBungeeMessage(String channel, Player player, String message, String lineFormat) {

        Channel lunaChatChannel = LunaChat.getInstance().getLunaChatAPI().getChannel(channel);
        if (lunaChatChannel == null) return;

        // isBungeeがtrueならバンジーコードにチャットを送信する。
        if (lunaChatChannel.isBungee()) {
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteOutStream);
            try {
                out.writeUTF(channel);
                out.writeUTF(player.getName());
                out.writeUTF(message);
                if (lineFormat == null) lineFormat = "";
                out.writeUTF(lineFormat);
                player.sendPluginMessage(LunaChat.getInstance(), "lunachat:in", byteOutStream.toByteArray());
                out.close();
                byteOutStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}