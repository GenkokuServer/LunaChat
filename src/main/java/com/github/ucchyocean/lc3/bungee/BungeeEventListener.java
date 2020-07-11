/*
  * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2020
 */
package com.github.ucchyocean.lc3.bungee;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.ucchyocean.lc3.LunaChat;
import com.github.ucchyocean.lc3.LunaChatAPI;
import com.github.ucchyocean.lc3.LunaChatBungee;
import com.github.ucchyocean.lc3.LunaChatConfig;
import com.github.ucchyocean.lc3.Messages;
import com.github.ucchyocean.lc3.channel.Channel;
import com.github.ucchyocean.lc3.event.EventResult;
import com.github.ucchyocean.lc3.japanize.Japanizer;
import com.github.ucchyocean.lc3.member.ChannelMember;
import com.github.ucchyocean.lc3.util.BungeeMessagingSubChannel;
import com.github.ucchyocean.lc3.util.ChatColor;
import com.github.ucchyocean.lc3.util.ClickableFormat;
import com.github.ucchyocean.lc3.util.Utility;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

/**
 * BungeeCordのイベントを監視するリスナークラス
 * @author ucchy
 */
public class BungeeEventListener implements Listener {

    private static final int MAX_LIST_ITEMS = 8;

    private LunaChatBungee parent;
    private LunaChatConfig config;
    private LunaChatAPI api;

    /**
     * コンストラクタ
     * @param parent LunaChatBungeeのインスタンス
     */
    public BungeeEventListener(LunaChatBungee parent) {
        this.parent = parent;
        config = parent.getConfig();
        api = parent.getLunaChatAPI();
    }

    /**
     * プレイヤーがチャット発言したときに呼び出されるメソッド
     * @param event
     */
    @EventHandler(priority=EventPriority.LOWEST)
    public void onAsyncPlayerChatLowest(ChatEvent event) {
        if ( matchesEventPriority(EventPriority.LOWEST)  && !config.isBungeeClientServerMode() ) {
            processChatEvent(event);
        }
    }

    /**
     * プレイヤーがチャット発言したときに呼び出されるメソッド
     * @param event
     */
    @EventHandler(priority=EventPriority.LOW)
    public void onAsyncPlayerChatLow(ChatEvent event) {
        if ( matchesEventPriority(EventPriority.LOW) && !config.isBungeeClientServerMode() ) {
            processChatEvent(event);
        }
    }

    /**
     * プレイヤーがチャット発言したときに呼び出されるメソッド
     * @param event
     */
    @EventHandler(priority=EventPriority.NORMAL)
    public void onAsyncPlayerChatNormal(ChatEvent event) {
        if ( matchesEventPriority(EventPriority.NORMAL) && !config.isBungeeClientServerMode() ) {
            processChatEvent(event);
        }
    }

    /**
     * プレイヤーがチャット発言したときに呼び出されるメソッド
     * @param event
     */
    @EventHandler(priority=EventPriority.HIGH)
    public void onAsyncPlayerChatHigh(ChatEvent event) {
        if ( matchesEventPriority(EventPriority.HIGH) && !config.isBungeeClientServerMode() ) {
            processChatEvent(event);
        }
    }

    /**
     * プレイヤーがチャット発言したときに呼び出されるメソッド
     * @param event
     */
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onAsyncPlayerChatHighest(ChatEvent event) {
        if ( matchesEventPriority(EventPriority.HIGHEST) && !config.isBungeeClientServerMode() ) {
            processChatEvent(event);
        }
    }

    @EventHandler
    public void onPluginMessageReceived(PluginMessageEvent event) {
        if (!event.getTag().equalsIgnoreCase("lunachat:bukkit")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String subChannel = in.readUTF();
        if (subChannel.equalsIgnoreCase(BungeeMessagingSubChannel.CHAT.name())) {
            String playerName = in.readUTF();
            String message = in.readUTF();

            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerName);
            if (player != null){
                processChatEvent(new ChatEvent(player,player.getServer(),message));
            }
        }
        if (subChannel.equalsIgnoreCase(BungeeMessagingSubChannel.SPIGOT_JOINED.name())){
            String playerName = in.readUTF();
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerName);
            ChannelMember channelMember = ChannelMember.getChannelMember(player.getName());



            List<Channel> leaveList = new ArrayList<>();

            for ( Channel channel : LunaChat.getAPI().getChannels() ) {
                if (channel.getForcedJoinedMembers().contains(channelMember)){
                    leaveList.add(channel);
                }
            }

            leaveList.forEach(channel -> channel.removeMember(channelMember));

            // 強制参加チャンネル設定を確認し、参加させる
            forceJoinToForceJoinChannels(player);

			// グローバルチャンネル設定がある場合
			if (!"".equals(channelMember.getGlobalChannelName())) {
				tryJoinToGlobalChannel(channelMember);
			}


            // チャンネルチャット情報を表示する
            if ( config.isShowListOnJoin() ) {
                for ( BaseComponent[] msg : getListForMotd(player) ) {
                    player.sendMessage(msg);
                }
            }
        }
    }

    /**
     * プレイヤーが接続したときに呼び出されるメソッド
     * @param event プレイヤーログインイベント
     */
    @EventHandler
    public void onJoin(PostLoginEvent event) {

        LunaChatConfig config = LunaChat.getConfig();
        ProxiedPlayer player = event.getPlayer();
        ChannelMember channelMember = ChannelMember.getChannelMember(player.getName());

        // UUIDをキャッシュ
        LunaChat.getUUIDCacheData().put(player.getUniqueId().toString(), player.getName());
        LunaChat.getUUIDCacheData().save();

        // 強制参加チャンネル設定を確認し、参加させる
        forceJoinToForceJoinChannels(player);

        if (channelMember != null){
            // グローバルチャンネル設定がある場合
            if ( !channelMember.getGlobalChannelName().equals("") ) {
                tryJoinToGlobalChannel(channelMember);
            }
        }

        // チャンネルチャット情報を表示する
        if ( config.isShowListOnJoin() && config.isBungeeClientServerMode()) {
            for ( BaseComponent[] msg : getListForMotd(player) ) {
                player.sendMessage(msg);
            }
        }
    }

    /**
     * プレイヤーのサーバー退出ごとに呼び出されるメソッド
     * @param event プレイヤー退出イベント
     */
    @EventHandler
    public void onQuit(PlayerDisconnectEvent event) {

        ProxiedPlayer player = event.getPlayer();
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
    private void processChatEvent(ChatEvent event) {

        // コマンド実行の場合は、そのまま無視する
        if ( event.isCommand() ) {
            return;
        }

        // プレイヤーの発言ではない場合は、そのまま無視する
        if ( !(event.getSender() instanceof ProxiedPlayer) ) {
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
                if ( channel != null && !channel.isPersonalChat()
                        && event.getSender() instanceof ProxiedPlayer ) {
                    ChannelMember player =
                            ChannelMember.getChannelMember(event.getSender());
                    if ( !channel.getMembers().contains(player) ) {
                        // 指定されたチャンネルに参加していないなら、エラーを表示して何も発言せずに終了する。
                        ((ProxiedPlayer)event.getSender()).sendMessage(
                                TextComponent.fromLegacyText(Messages.errmsgNomember()));
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
                ChannelMember.getChannelMember(event.getSender());
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

    private void chatGlobal(ChatEvent event) {

        // 発言者と発言サーバーと発言内容の取得
        final ProxiedPlayer sender = (ProxiedPlayer)event.getSender();
        String message = event.getMessage();
        LunaChatConfig config = LunaChat.getConfig();

        // NGワードのマスク
        message = maskNGWord(message, config.getNgwordCompiled());

        // Japanizeをスキップするかどうかフラグ
        boolean skipJapanize = !LunaChat.getAPI().isPlayerJapanize(sender.getName());

        // 一時的なJapanizeスキップが指定されているか確認する
        String marker = config.getNoneJapanizeMarker();
        if ( !marker.equals("") && message.startsWith(marker) ) {
            message = message.substring(marker.length());
            skipJapanize = true;
        }

        // 2byteコードを含む、または、半角カタカナのみなら、Japanize変換は行わない
        String kanaTemp = Utility.stripColorCode(message);

        if ( !skipJapanize &&
                ( kanaTemp.getBytes(StandardCharsets.UTF_8).length > kanaTemp.length() ||
                        kanaTemp.matches("[ \\uFF61-\\uFF9F]+") ) ) {
            skipJapanize = true;
        }

        // Japanizeの付加
        if ( !skipJapanize ) {

            String japanize = Japanizer.japanize(Utility.stripColorCode(message), config.getJapanizeType(),
                    LunaChat.getAPI().getAllDictionary());
            if ( japanize.length() > 0 ) {

                // NGワードのマスク
                japanize = maskNGWord(japanize, config.getNgwordCompiled());

                // フォーマット化してメッセージを上書きする
                String japanizeFormat = config.getJapanizeDisplayLine() == 1 ?
                        config.getJapanizeLine1Format() :
                        "%msg\n" + config.getJapanizeLine2Format();
                String preMessage = new String(message);
                message = japanizeFormat.replace("%msg", preMessage).replace("%japanize", japanize);
            }
        }

        String result;

        if ( config.isEnableNormalChatMessageFormat() ) {
            // チャットフォーマット装飾を適用する場合
            String f = config.getNormalChatMessageFormat();
            ClickableFormat format = ClickableFormat.makeFormat(f, ChannelMember.getChannelMember(sender));
            format.replace("%msg", message);

            // イベントをキャンセルする
            event.setCancelled(true);

            // hideされているプレイヤーを除くすべてのプレイヤーに、
            // 発言内容を送信する。
            BaseComponent[] msg = format.makeTextComponent();
            List<ChannelMember> hidelist = api.getHidelist(ChannelMember.getChannelMember(sender));

            for ( String server : parent.getProxy().getServers().keySet() ) {

                ServerInfo info = parent.getProxy().getServerInfo(server);
                for ( ProxiedPlayer player : info.getPlayers() ) {
                    if ( !containsHideList(player, hidelist) ) {
                        sendMessage(player, msg);
                    }
                }
            }

            result = format.toLegacyText();

        } else {
            // チャットフォーマットを適用しない場合
            // NOTE: ChatEvent経由で送信する都合上、hideは適用しない（できない）仕様とする。

            // NOTE: 改行がサポートされないので、改行を含む場合は、
            // \nで分割して前半をセットし、後半は150ミリ秒後に送信する。
            if ( !message.contains("\n") ) {
                event.setMessage(Utility.stripColorCode(message));
            } else {
                int index = message.indexOf("\n");
                String pre = message.substring(0, index);
                final String post = Utility.replaceColorCode(
                        message.substring(index + "\n".length()));
                event.setMessage(Utility.stripColorCode(pre));

                parent.getProxy().getScheduler().schedule(parent, new Runnable() {
                    @Override
                    public void run() {
                        for ( ProxiedPlayer p : sender.getServer().getInfo().getPlayers() ) {
                            sendMessage(p, post);
                        }
                    }
                }, 150, TimeUnit.MILLISECONDS);
            }

            result = Utility.replaceColorCode(message);

            // 発言したプレイヤーがいるサーバー"以外"のサーバーに、
            // 発言内容を送信する。
            for ( String server : parent.getProxy().getServers().keySet() ) {

                String senderServer = sender.getServer().getInfo().getName();
                if ( server.equals(senderServer) ) {
                    continue;
                }

                ServerInfo info = parent.getProxy().getServerInfo(server);
                for ( ProxiedPlayer player : info.getPlayers() ) {
                    sendMessage(player, result);
                }
            }
        }

        // コンソールに表示設定なら、コンソールに表示する
        if ( config.isDisplayChatOnConsole() ) {
            parent.getLogger().info(result);
        }

        // ログに記録する
        LunaChat.getNormalChatLogger().log(Utility.stripColorCode(result), sender.getName());
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
     * NGワードをマスクする
     * @param message メッセージ
     * @param ngwords NGワード
     * @return マスクされたメッセージ
     */
    private String maskNGWord(String message, List<Pattern> ngwords) {
        for ( Pattern pattern : ngwords ) {
            Matcher matcher = pattern.matcher(message);
            if ( matcher.find() ) {
                message = matcher.replaceAll(
                        Utility.getAstariskString(matcher.group(0).length()));
            }
        }
        return message;
    }

    /**
     * 強制参加チャンネルへ参加させる
     * @param player プレイヤー
     */
    private void forceJoinToForceJoinChannels(ProxiedPlayer player) {

        LunaChatAPI api = LunaChat.getAPI();
        ChannelMember channelMember = ChannelMember.getChannelMember(player);
        if (channelMember != null) {
            List<String> forceJoinChannels = channelMember.getForceJoinChannels();

            for ( String cname : forceJoinChannels ) {
                // チャンネルが存在しない場合は作成する
                Channel channel = api.getChannel(cname);
                if ( channel == null ) {
                    channel = api.createChannel(cname);
                }

                // チャンネルのメンバーでないなら、参加する
                ChannelMember cp = ChannelMember.getChannelMember(player);
                if ( !channel.getMembers().contains(cp) ) {
                    channel.addMemberFromForceJoin(cp);
                }
            }
        }
    }

    /**
     * 既定のチャンネルへの参加を試みる。
     * @param player プレイヤー
     * @return 参加できたかどうか
     */
	private boolean tryJoinToGlobalChannel(ChannelMember cp) {

		LunaChatConfig config = LunaChat.getConfig();
		LunaChatAPI api = LunaChat.getAPI();
		String gcName = cp.getGlobalChannelName();

		// チャンネルが存在しない場合は作成する
		Channel global = api.getChannel(gcName);
		if (global == null) {
			global = api.createChannel(gcName);
		}

		// チャンネルのメンバーでないなら、参加する
		if (!global.getMembers().contains(cp)) {
			global.addMemberFromForceJoin(cp);
		}

		// デフォルト発言先が無いなら、グローバルチャンネルに設定する
		Channel dchannel = api.getDefaultChannel(cp.getName());
		if (dchannel == null || config.isGlobalChannel(dchannel.getName())) {
			api.setDefaultChannel(cp.getName(), gcName);
		}
		return true;

	}

    /**
     * プレイヤーのサーバー参加時用の参加チャンネルリストを返す
     * @param player プレイヤー
     * @return リスト
     */
    private ArrayList<BaseComponent[]> getListForMotd(ProxiedPlayer player) {

        ChannelMember cp = ChannelMember.getChannelMember(player);
        LunaChatAPI api = LunaChat.getAPI();
        Channel dc = api.getDefaultChannel(cp.getName());
        String dchannel = "";
        if ( dc != null ) {
            dchannel = dc.getName().toLowerCase();
        }

        // チャンネル一覧を取得して、参加人数でソートする
        ArrayList<Channel> channels = new ArrayList<>(api.getChannels());
        Collections.sort(channels, new Comparator<Channel>() {
            public int compare(Channel c1, Channel c2) {
                if ( c1.getOnlineNum() == c2.getOnlineNum() ) return c1.getName().compareTo(c2.getName());
                return c2.getOnlineNum() - c1.getOnlineNum();
            }
        });

        int count = 0;
        ArrayList<BaseComponent[]> items = new ArrayList<>();
        items.add(TextComponent.fromLegacyText(Messages.motdFirstLine()));
        for ( Channel channel : channels ) {

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
            items.add(Messages.listFormat(disp, onlineNum, memberNum, desc));
            count++;

            if ( count > MAX_LIST_ITEMS ) {
                break;
            }
        }
        items.add(TextComponent.fromLegacyText(Messages.listEndLine()));

        return items;
    }

    /**
     * 指定した対象にメッセージを送信する
     * @param reciever 送信先
     * @param message メッセージ
     */
    private void sendMessage(CommandSender reciever, String message) {
        if ( message == null ) return;
        ChannelMember cm = ChannelMember.getChannelMember(reciever);
        if ( cm != null ) {
            cm.sendMessage(message);
        }
    }

    /**
     * 指定した対象にメッセージを送信する
     * @param reciever 送信先
     * @param message メッセージ
     */
    private void sendMessage(CommandSender reciever, BaseComponent[] message) {
        if ( message == null ) return;
        ChannelMember cm = ChannelMember.getChannelMember(reciever);
        if ( cm != null ) {
            cm.sendMessage(message);
        }
    }

    /**
     * 指定されたEventPriorityが、LunaChatConfigで指定されているEventPriorityと一致するかどうかを調べる
     * @param priority
     * @return 一致するかどうか
     */
    private boolean matchesEventPriority(int priority) {
        String c = LunaChat.getConfig().getPlayerChatEventListenerPriority().name();
        if ( priority == EventPriority.LOWEST ) return "LOWEST".equals(c);
        if ( priority == EventPriority.LOW ) return "LOW".equals(c);
        if ( priority == EventPriority.NORMAL ) return "NORMAL".equals(c);
        if ( priority == EventPriority.HIGH ) return "HIGH".equals(c);
        if ( priority == EventPriority.HIGHEST ) return "HIGHEST".equals(c);
        return false;
    }

    /**
     * 指定されたプレイヤーが指定されたHideListに含まれるかどうかを判定する
     * @param p プレイヤー
     * @param list リスト
     * @return 含まれるかどうか
     */
    private boolean containsHideList(ProxiedPlayer p, List<ChannelMember> list) {
        for ( ChannelMember m : list ) {
            if (m.getName().equals(p.getName())) return true;
        }
        return false;
    }
}