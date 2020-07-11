package com.github.ucchyocean.lc3.bridge;

import com.github.ucchyocean.lc3.LunaChatBukkit;
import com.github.ucchyocean.lc3.util.BungeeMessagingSubChannel;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BungeeBridge {

    public static final String BUNGEE_CHANNEL = "lunachat:bungee";
    public static final String BUKKIT_CHANNEL = "lunachat:bukkit";

    private BungeeBridge(){}

    public static void sendBungeeMessage(Player player, String text) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(BungeeMessagingSubChannel.CHAT.name());
        out.writeUTF(player.getName());
        out.writeUTF(text);

        Player p = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (p != null) {
            try {
                p.sendPluginMessage(LunaChatBukkit.getInstance(), BUKKIT_CHANNEL, out.toByteArray());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void sendBungeeOnPlayerJoined(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(BungeeMessagingSubChannel.SPIGOT_JOINED.name());
        out.writeUTF(player.getName());

        Player p = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (p != null) {
            try {
                p.sendPluginMessage(LunaChatBukkit.getInstance(), BUKKIT_CHANNEL, out.toByteArray());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
