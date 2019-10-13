package com.github.ucchyocean.lc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.github.ucchyocean.lc.channel.Channel;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class PluginMessageChannelManager implements PluginMessageListener {

    private static final LunaChat lunaChat = LunaChat.getInstance();

    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, @NotNull byte[] message) {
        if (!channel.equalsIgnoreCase("lunachat:out")) return;

        ByteArrayInputStream byteIn = new ByteArrayInputStream(message);
        DataInputStream in = new DataInputStream(byteIn);

        try {
            String lunaChatChannelName = in.readUTF();
            Channel lunaChatChannel = lunaChat.getLunaChatAPI().getChannel(lunaChatChannelName);
            if (lunaChatChannel == null){
                return;
            }
            
            String sender = in.readUTF();
            if (sender == null){
                return;
            }

            String chat = in.readUTF();
            if (chat == null){
                return;
            }

            String lineFormat = in.readUTF();

            String serverName = in.readUTF();
            if (serverName == null){
                return;
            }

            if (!lineFormat.equals("")){
                lunaChatChannel.chatFromOtherSource(sender, serverName, chat, false);
            }
            else{
                lunaChatChannel.sendMessage(null, chat, null, false, sender, true);
            }

            in.close();
            byteIn.close();

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void sendBungeeMessage(String channel, Player player, String message, String lineFormat){

        Channel lunaChatChannel = lunaChat.getLunaChatAPI().getChannel(channel);
        if (lunaChatChannel == null) return;

        // isBungeeがtrueならバンジーコードにチャットを送信する。
        if (lunaChatChannel.isBungee()){
			ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(byteOutStream);
			try {
                out.writeUTF(channel);
                out.writeUTF(player.getName());
                out.writeUTF(message);
                if (lineFormat == null) lineFormat = "";
				out.writeUTF(lineFormat);
				player.sendPluginMessage(Bukkit.getPluginManager().getPlugin("LunaChat"), "lunachat:in", byteOutStream.toByteArray());
                out.close();
                byteOutStream.close();
			}
			catch(Exception e) {
                e.printStackTrace();
            }

        }
    }
}