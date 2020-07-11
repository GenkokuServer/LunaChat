/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2020
 */
package com.github.ucchyocean.lc3.bukkit;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.ucchyocean.lc3.bridge.BungeeBridge;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.LunaChatAPI;
import com.github.ucchyocean.lc3.LunaChatBukkit;
import com.github.ucchyocean.lc3.LunaChatConfig;
import com.github.ucchyocean.lc3.Messages;
import com.github.ucchyocean.lc3.bridge.VaultChatBridge;
import com.github.ucchyocean.lc3.channel.Channel;
import com.github.ucchyocean.lc3.event.EventResult;
import com.github.ucchyocean.lc3.japanize.JapanizeType;
import com.github.ucchyocean.lc3.member.ChannelMember;
import com.github.ucchyocean.lc3.member.ChannelMemberBukkit;
import com.github.ucchyocean.lc3.util.KeywordReplacer;
import com.github.ucchyocean.lc3.util.Utility;

/**
 * Bukkit関連のイベントを監視するリスナ
 * @author ucchy
 */
public class BukkitEventListener implements Listener {

    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    /**
     * コンストラクタ
     */
    public BukkitEventListener() {
        dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        timeFormat = new SimpleDateFormat("HH:mm:ss");
    }

    /**
     * プレイヤーがチャット発言したときに呼び出されるメソッド
     * @param event
     */
    @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
    public void onAsyncPlayerChatLowest(AsyncPlayerChatEvent event) {
        if ( matchesEventPriority(EventPriority.LOWEST) ) {
            processChatEvent(event);
        }
    }

    /**
     * プレイヤーがチャット発言したときに呼び出されるメソッド
     * @param event
     */
    @EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
    public void onAsyncPlayerChatLow(AsyncPlayerChatEvent event) {
        if ( matchesEventPriority(EventPriority.LOW) ) {
            processChatEvent(event);
        }
    }

    /**
     * プレイヤーがチャット発言したときに呼び出されるメソッド
     * @param event
     */
    @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
    public void onAsyncPlayerChatNormal(AsyncPlayerChatEvent event) {
        if ( matchesEventPriority(EventPriority.NORMAL) ) {
            processChatEvent(event);
        }
    }

    /**
     * プレイヤーがチャット発言したときに呼び出されるメソッド
     * @param event
     */
    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onAsyncPlayerChatHigh(AsyncPlayerChatEvent event) {
        if ( matchesEventPriority(EventPriority.HIGH) ) {
            processChatEvent(event);
        }
    }

    /**
     * プレイヤーがチャット発言したときに呼び出されるメソッド
     * @param event
     */
    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onAsyncPlayerChatHighest(AsyncPlayerChatEvent event) {
        if ( matchesEventPriority(EventPriority.HIGHEST) ) {
            processChatEvent(event);
        }
    }

    /**
     * プレイヤーのサーバー参加ごとに呼び出されるメソッド
     * @param event プレイヤー参加イベント
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        LunaChatConfig config = LunaChat.getConfig();
        Player player = event.getPlayer();
        ChannelMember channelMember = ChannelMember.getChannelMember(player.getName());

        if (config.isBungeeClientServerMode()) {
            BungeeBridge.sendBungeeOnPlayerJoined(player);
            return;
        }

        // UUIDをキャッシュ
        LunaChat.getUUIDCacheData().put(player.getUniqueId().toString(), player.getName());
        LunaChat.getUUIDCacheData().save();

        if (channelMember != null){
            // 強制参加チャンネル設定を確認し、参加させる
            forceJoinToForceJoinChannels(channelMember);

            // グローバルチャンネル設定がある場合
            if (!channelMember.getGlobalChannelName().equals("")) {
                tryJoinToGlobalChannel(channelMember);
            }
        }

        // チャンネルチャット情報を表示する
        if ( config.isShowListOnJoin() ) {
            ArrayList<String> list = getListForMotd(player);
            for ( String msg : list ) {
                player.sendMessage(msg);
            }
        }
    }

    /**
     * プレイヤーのサーバー退出ごとに呼び出されるメソッド
     * @param event プレイヤー退出イベント
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        LunaChatConfig config = LunaChat.getConfig();

        if (config.isBungeeClientServerMode()) {
            return;
        }

        Player player = event.getPlayer();
        String pname = player.getName();

        // お互いがオフラインになるPMチャンネルがある場合は
        // チャンネルをクリアする
        ArrayList<Channel> deleteList = new ArrayList<Channel>();

        for ( Channel channel : LunaChat.getAPI().getChannels() ) {
            String cname = channel.getName();
            if ( channel.isPersonalChat() && cname.contains(pname) ) {
                boolean isAllOffline = true;
                for ( ChannelMember cp : channel.getMembers() ) {
                    if ( cp.isOnline() ) {
                        isAllOffline = false;
                    }
                }
                if ( isAllOffline ) {
                    deleteList.add(channel);
                }
            }
        }

        for ( Channel channel : deleteList ) {
            LunaChat.getAPI().removeChannel(channel.getName());
        }
    }

    /**
     * プレイヤーのチャットごとに呼び出されるメソッド
     * @param event チャットイベント
     */
    private void processChatEvent(AsyncPlayerChatEvent event) {

        LunaChatConfig config = LunaChat.getConfig();
        LunaChatAPI api = LunaChat.getAPI();

        if (config.isBungeeClientServerMode()) {
            BungeeBridge.sendBungeeMessage(event.getPlayer(), event.getMessage());
            event.setCancelled(true);
            return;
        }

        // 頭にglobalMarkerが付いている場合は、グローバル発言にする
        if ( config.getGlobalMarker() != null &&
                !config.getGlobalMarker().equals("") &&
                event.getMessage().startsWith(config.getGlobalMarker()) &&
                event.getMessage().length() > config.getGlobalMarker().length() ) {

            int offset = config.getGlobalMarker().length();
            event.setMessage( event.getMessage().substring(offset) );
            chatGlobal(event);
            return;
        }

        // クイックチャンネルチャット機能が有効で、専用の記号が含まれるなら、
        // クイックチャンネルチャットとして処理する。
        if ( config.isEnableQuickChannelChat() ) {
            String separator = config.getQuickChannelChatSeparator();
            if ( event.getMessage().contains(separator) ) {
                String[] temp = event.getMessage().split(separator, 2);
                String name = temp[0];
                String value = "";
                if ( temp.length > 0 ) {
                    value = temp[1];
                }

                Channel channel = api.getChannel(name);
                if ( channel != null && !channel.isPersonalChat() ) {
                    ChannelMember player =
                            ChannelMember.getChannelMember(event.getPlayer());
                    if ( !channel.getMembers().contains(player) ) {
                        // 指定されたチャンネルに参加していないなら、エラーを表示して何も発言せずに終了する。
                        event.getPlayer().sendMessage(Messages.errmsgNomember());
                        event.setCancelled(true);
                        return;
                    }

                    // 指定されたチャンネルに発言して終了する。
                    chatToChannelWithEvent(player, channel, value);
                    event.setCancelled(true);
                    return;
                }
            }
        }

        ChannelMember player =
                ChannelMember.getChannelMember(event.getPlayer());
        Channel channel = api.getDefaultChannel(player.getName());

        // デフォルトの発言先が無い場合
        if ( channel == null ) {
            if ( config.isNoJoinAsGlobal() ) {
                // グローバル発言にする
                chatGlobal(event);
                return;

            } else {
                // 発言をキャンセルして終了する
                event.setCancelled(true);
                return;
            }
        }

        chatToChannelWithEvent(player, channel, event.getMessage());

        // もとのイベントをキャンセル
        event.setCancelled(true);
    }

    /**
     * イベントをグローバルチャット発言として処理する
     * @param event 処理するイベント
     */
    private void chatGlobal(AsyncPlayerChatEvent event) {

        LunaChatConfig config = LunaChat.getConfig();
        LunaChatAPI api = LunaChat.getAPI();
        ChannelMember player =
                ChannelMember.getChannelMember(event.getPlayer());
        String globalChannelName = player != null ? player.getGlobalChannelName() : config.getGlobalChannel(LunaChatConfig.DEFAULT_SERVER_NAME);

        if ( !"".equals(globalChannelName) ) {
            // グローバルチャンネル設定がある場合

            // グローバルチャンネルの取得、無ければ作成
            Channel global = api.getChannel(globalChannelName);
            if ( global == null ) {
                global = api.createChannel(globalChannelName);
            }

            // LunaChatPreChatEvent イベントコール
            EventResult result = LunaChat.getEventSender().sendLunaChatPreChatEvent(
                    global.getName(), player, event.getMessage());
            if ( result.isCancelled() ) {
                event.setCancelled(true);
                return;
            }
            Channel alt = result.getChannel();
            if ( alt != null ) {
                global = alt;
            }
            String message = result.getMessage();

            // デフォルト発言先が無いなら、グローバルチャンネルに設定する
            Channel dchannel = api.getDefaultChannel(player.getName());
            if ( dchannel == null ) {
                api.setDefaultChannel(player.getName(), global.getName());
            }

            // チャンネルチャット発言
            chatToChannelWithEvent(player, global, message);

            // もとのイベントをキャンセル
            event.setCancelled(true);

            return;

        } else {
            // グローバルチャンネル設定が無い場合

            String message = event.getMessage();
            // NGワード発言をマスク
            for ( Pattern pattern : config.getNgwordCompiled() ) {
                Matcher matcher = pattern.matcher(message);
                if ( matcher.find() ) {
                    message = matcher.replaceAll(
                            Utility.getAstariskString(matcher.group(0).length()));
                }
            }

            // チャットフォーマット装飾の適用
            if ( config.isEnableNormalChatMessageFormat() ) {
                String format = config.getNormalChatMessageFormat();
                format = replaceNormalChatFormatKeywords(format, event.getPlayer());
                event.setFormat(format);
            }

            // カラーコード置き換え
            // 置き換え設定になっていて、発言者がパーミッションを持っているなら、置き換えする
            if ( config.isEnableNormalChatColorCode() &&
                    event.getPlayer().hasPermission("lunachat.allowcc") ) {
                message = Utility.replaceColorCode(message);
            }

            // hideされているプレイヤーを、recipientから抜く
            for ( ChannelMember cp : api.getHidelist(player) ) {
                Player p = ((ChannelMemberBukkit)cp).getPlayer();
                if ( p != null ) {
                    event.getRecipients().remove(p);
                }
            }

            // 一時的にJapanizeスキップ設定かどうかを確認する
            boolean skipJapanize = false;
            String marker = config.getNoneJapanizeMarker();
            if ( !marker.equals("") && message.startsWith(marker) ) {
                skipJapanize = true;
                message = message.substring(marker.length());
            }

            // 2byteコードを含む、または、半角カタカナのみなら、Japanize変換は行わない
            String kanaTemp = Utility.stripColorCode(message);
            if ( !skipJapanize &&
                    ( kanaTemp.getBytes(StandardCharsets.UTF_8).length > kanaTemp.length() ||
                            kanaTemp.matches("[ \\uFF61-\\uFF9F]+") ) ) {
                skipJapanize = true;
            }

            // Japanize変換と、発言処理
            if ( !skipJapanize &&
                    LunaChat.getAPI().isPlayerJapanize(player.getName()) &&
                    config.getJapanizeType() != JapanizeType.NONE ) {

                int lineType = config.getJapanizeDisplayLine();

                if ( lineType == 1 ) {

                    String taskFormat = Utility.replaceColorCode(config.getJapanizeLine1Format());

                    String japanized = api.japanize(
                            kanaTemp, config.getJapanizeType());
                    if ( japanized != null ) {
                        String temp = taskFormat.replace("%msg", message);
                        message = temp.replace("%japanize", japanized);
                    }

                } else {

                    String taskFormat = Utility.replaceColorCode(config.getJapanizeLine2Format());

                    BukkitNormalChatJapanizeTask task = new BukkitNormalChatJapanizeTask(
                            message, config.getJapanizeType(), player, taskFormat, event);

                    // 発言処理を必ず先に実施させるため、遅延を入れてタスクを実行する。
                    int wait = config.getJapanizeWait();
                    task.runTaskLater(LunaChatBukkit.getInstance(), wait);
                }
            }

            // 発言内容の設定
            event.setMessage(message);

            // ロギング
            logNormalChat(message, player.getName());

            return;
        }
    }

    /**
     * 既定のチャンネルへの参加を試みる。
     * @param player プレイヤー
     * @return 参加できたかどうか
     */
    private boolean tryJoinToGlobalChannel(ChannelMember channelMember) {

        LunaChatAPI api = LunaChat.getAPI();
        String gcName = channelMember.getGlobalChannelName();

        // チャンネルが存在しない場合は作成する
        Channel global = api.getChannel(gcName);
        if ( global == null ) {
            global = api.createChannel(gcName);
        }

        // チャンネルのメンバーでないなら、参加する
        if ( !global.getMembers().contains(channelMember) ) {
            global.addMemberFromForceJoin(channelMember);
        }

        // デフォルト発言先が無いなら、グローバルチャンネルに設定する
        Channel dchannel = api.getDefaultChannel(channelMember.getName());
        if ( dchannel == null ) {
            api.setDefaultChannel(channelMember.getName(), gcName);
        }

        return true;
    }

    /**
     * 強制参加チャンネルへ参加させる
     * @param channelMember プレイヤー
     */
    private void forceJoinToForceJoinChannels(ChannelMember channelMember) {

        LunaChatAPI api = LunaChat.getAPI();

        List<String> forceJoinChannels = channelMember.getForceJoinChannels();

        for ( String cname : forceJoinChannels ) {

            // チャンネルが存在しない場合は作成する
            Channel channel = api.getChannel(cname);
            if ( channel == null ) {
                channel = api.createChannel(cname);
            }

            // チャンネルのメンバーでないなら、参加する
            ChannelMember cp = ChannelMember.getChannelMember(channelMember);
            if ( !channel.getMembers().contains(cp) ) {
                channel.addMemberFromForceJoin(cp);
            }
        }
    }

    /**
     * 通常チャットのフォーマット設定のキーワードを置き換えして返す
     * @param org フォーマット設定
     * @param player 発言プレイヤー
     * @return キーワード置き換え済みの文字列
     */
    private String replaceNormalChatFormatKeywords(String org, Player player) {

        KeywordReplacer format = new KeywordReplacer(org);
        format.replace("%username", "%1$s");
        format.replace("%msg", "%2$s");
        format.replace("%player", player.getName());

        if ( format.contains("%date") ) {
            format.replace("%date", dateFormat.format(new Date()));
        }
        if ( format.contains("%time") ) {
            format.replace("%time", timeFormat.format(new Date()));
        }

        if ( format.contains("%prefix") || format.contains("%suffix") ) {

            String prefix = "";
            String suffix = "";
            VaultChatBridge vaultchat = LunaChatBukkit.getInstance().getVaultChat();
            if ( vaultchat != null ) {
                prefix = vaultchat.getPlayerPrefix(player);
                suffix = vaultchat.getPlayerSuffix(player);
            }
            format.replace("%prefix", prefix);
            format.replace("%suffix", suffix);
        }

        if ( format.contains("%world") ) {

            String worldname = null;
            if ( LunaChatBukkit.getInstance().getMultiverseCore() != null ) {
                worldname = LunaChatBukkit.getInstance().getMultiverseCore().getWorldAlias(player.getWorld());
            }
            if ( worldname == null || worldname.equals("") ) {
                worldname = player.getWorld().getName();
            }
            format.replace("%world", worldname);
        }

        format.replace("%server", "");

        return Utility.replaceColorCode(format.toString());
    }

    /**
     * プレイヤーのサーバー参加時用の参加チャンネルリストを返す
     * @param player プレイヤー
     * @return リスト
     */
    private ArrayList<String> getListForMotd(Player player) {

        ChannelMember cp = ChannelMember.getChannelMember(player);
        LunaChatAPI api = LunaChat.getAPI();
        Channel dc = api.getDefaultChannel(cp.getName());
        String dchannel = "";
        if ( dc != null ) {
            dchannel = dc.getName().toLowerCase();
        }

        ArrayList<String> items = new ArrayList<String>();
        items.add(Messages.motdFirstLine());
        for ( Channel channel : api.getChannels() ) {

            // BANされているチャンネルは表示しない
            if ( channel.getBanned().contains(cp) ) {
                continue;
            }

            // 個人チャットはリストに表示しない
            if ( channel.isPersonalChat() ) {
                continue;
            }

            // 参加していないチャンネルは、グローバルチャンネルを除き表示しない
            if ( !channel.getMembers().contains(cp) &&
                    !channel.isGlobalChannel(cp) ) {
                continue;
            }

            String disp = ChatColor.WHITE + channel.getName();
            if ( channel.getName().equals(dchannel) ) {
                disp = ChatColor.RED + channel.getName();
            }
            String desc = channel.getDescription();
            int onlineNum = channel.getOnlineNum();
            int memberNum = channel.getTotalNum();
            String item = Messages.listFormat(disp, onlineNum, memberNum, desc);
            items.add(item);
        }
        items.add(Messages.listEndLine());

        return items;
    }

    /**
     * チャンネルに発言処理を行う
     * @param player プレイヤー
     * @param channel チャンネル
     * @param message 発言内容
     * @return イベントでキャンセルされたかどうか
     */
    private boolean chatToChannelWithEvent(ChannelMember player, Channel channel, String message) {

        // LunaChatPreChatEvent イベントコール
        EventResult result = LunaChat.getEventSender().sendLunaChatPreChatEvent(
                channel.getName(), player, message);
        if ( result.isCancelled() ) {
            return true;
        }
        Channel alt = result.getChannel();
        if ( alt != null ) {
            channel = alt;
        }
        message = result.getMessage();

        // チャンネルチャット発言
        channel.chat(player, message);

        return false;
    }

    /**
     * 通常チャットの発言をログファイルへ記録する
     * @param message
     * @param player
     */
    private void logNormalChat(String message, String player) {
        LunaChat.getNormalChatLogger().log(message, player);
    }

    /**
     * 指定されたEventPriorityが、LunaChatConfigで指定されているEventPriorityと一致するかどうかを調べる
     * @param priority
     * @return 一致するかどうか
     */
    private boolean matchesEventPriority(EventPriority priority) {
        String c = LunaChat.getConfig().getPlayerChatEventListenerPriority().name();
        return c.equals(priority.name());
    }
}
