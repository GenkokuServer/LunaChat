/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.lc.channel;

import com.github.ucchyocean.lc.LunaChat;
import com.github.ucchyocean.lc.LunaChatAPI;
import com.github.ucchyocean.lc.Resources;
import com.github.ucchyocean.lc.event.LunaChatChannelCreateEvent;
import com.github.ucchyocean.lc.event.LunaChatChannelRemoveEvent;
import com.github.ucchyocean.lc.japanize.JapanizeType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * チャンネルマネージャー
 *
 * @author ucchy
 */
public class ChannelManager implements LunaChatAPI {

    private static final String MSG_BREAKUP = Resources.get("breakupMessage");

    private static final Path FILE_NAME_DCHANNELS = LunaChat.getInstance().getDataFolder().toPath().resolve("defaults.yml");
    private static final Path FILE_NAME_TEMPLATES = LunaChat.getInstance().getDataFolder().toPath().resolve("templates.yml");
    private static final Path FILE_NAME_JAPANIZE = LunaChat.getInstance().getDataFolder().toPath().resolve("japanize.yml");
    private static final Path FILE_NAME_DICTIONARY = LunaChat.getInstance().getDataFolder().toPath().resolve("dictionary.yml");
    private static final Path FILE_NAME_HIDELIST = LunaChat.getInstance().getDataFolder().toPath().resolve("hidelist.yml");

    private HashMap<String, Channel> channels;
    private HashMap<String, String> defaultChannels;
    private HashMap<String, String> templates;
    private HashMap<String, Boolean> japanize;
    private HashMap<String, String> dictionary;
    private HashMap<String, List<ChannelPlayer>> hidelist;

    /**
     * コンストラクタ
     */
    public ChannelManager() {
        reloadAllData();
    }

    /**
     * すべて読み込みする
     */
    @Override
    public void reloadAllData() {

        // デフォルトチャンネル設定のロード
        defaultChannels = getSetting(FILE_NAME_DCHANNELS);

        // テンプレート設定のロード
        templates = getSetting(FILE_NAME_TEMPLATES);

        // dictionaryのロード
        dictionary = getSetting(FILE_NAME_DICTIONARY);

        // Japanize設定のロード
        if (!Files.exists(FILE_NAME_JAPANIZE)) {
            try {
                new YamlConfiguration().save(FILE_NAME_JAPANIZE.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        YamlConfiguration configJapanize = YamlConfiguration.loadConfiguration(FILE_NAME_JAPANIZE.toFile());
        japanize = new HashMap<>();
        configJapanize.getKeys(false).forEach(key -> japanize.put(key, configJapanize.getBoolean(key)));

        // hideリストのロード
        if (!Files.exists(FILE_NAME_HIDELIST)) {
            try {
                new YamlConfiguration().save(FILE_NAME_HIDELIST.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        YamlConfiguration configHidelist = YamlConfiguration.loadConfiguration(FILE_NAME_HIDELIST.toFile());
        hidelist = new HashMap<>();

        configHidelist.getKeys(false).forEach(key -> {
            hidelist.put(key, new ArrayList<>());
            configHidelist.getStringList(key).forEach(id -> hidelist.get(key).add(ChannelPlayer.getChannelPlayer(id)));
        });

        // チャンネル設定のロード
        channels = Channel.loadAllChannels();
    }

    private HashMap<String, String> getSetting(Path file) {
        if (!Files.exists(file)) {
            try {
                new YamlConfiguration().save(file.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file.toFile());

        HashMap<String, String> map = new HashMap<>();

        config.getKeys(false).forEach(key -> map.put(key, config.getString(key)));

        return map;
    }

    /**
     * デフォルトチャンネル設定を保存する
     */
    private void saveDefaults() {
        try {
            YamlConfiguration config = new YamlConfiguration();
            defaultChannels.keySet().forEach(key -> config.set(key, defaultChannels.get(key)));
            config.save(FILE_NAME_DCHANNELS.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * テンプレート設定を保存する
     */
    private void saveTemplates() {
        try {
            YamlConfiguration config = new YamlConfiguration();
            templates.keySet().forEach(key -> config.set(key, templates.get(key)));
            config.save(FILE_NAME_TEMPLATES.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Japanize設定を保存する
     */
    private void saveJapanize() {
        try {
            YamlConfiguration config = new YamlConfiguration();
            japanize.keySet().forEach(key -> config.set(key, japanize.get(key)));
            config.save(FILE_NAME_JAPANIZE.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Dictionary設定を保存する
     */
    private void saveDictionary() {
        try {
            YamlConfiguration config = new YamlConfiguration();
            dictionary.keySet().forEach(key -> config.set(key, dictionary.get(key)));
            config.save(FILE_NAME_DICTIONARY.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Hidelist設定を保存する
     */
    private void saveHidelist() {
        try {
            YamlConfiguration config = new YamlConfiguration();
            hidelist.keySet().forEach(key -> config.set(key, getIdList(hidelist.get(key))));
            config.save(FILE_NAME_HIDELIST.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * デフォルトチャンネル設定を全て削除する
     */
    public void removeAllDefaultChannels() {
        defaultChannels.clear();
        saveDefaults();
    }

    /**
     * プレイヤーのJapanize設定を返す
     *
     * @param playerName プレイヤー名
     * @return Japanize設定
     */
    @Override
    public boolean isPlayerJapanize(String playerName) {
        if (!japanize.containsKey(playerName)) {
            return true;
        }
        return japanize.get(playerName);
    }

    /**
     * 指定したチャンネル名が存在するかどうかを返す
     *
     * @param channelName チャンネル名
     * @return 存在するかどうか
     * @see com.github.ucchyocean.lc.LunaChatAPI#isExistChannel(java.lang.String)
     */
    @Override
    public boolean isExistChannel(String channelName) {
        if (channelName == null) {
            return false;
        }
        return channels.containsKey(channelName.toLowerCase());
    }

    /**
     * 全てのチャンネルを返す
     *
     * @return 全てのチャンネル
     * @see com.github.ucchyocean.lc.LunaChatAPI#getChannels()
     */
    @Override
    public Collection<Channel> getChannels() {

        return channels.values();
    }

    /**
     * プレイヤーが参加しているチャンネルを返す
     *
     * @param playerName プレイヤー名
     * @return チャンネル
     * @see com.github.ucchyocean.lc.LunaChatAPI#getChannelsByPlayer(java.lang.String)
     */
    @Override
    public Collection<Channel> getChannelsByPlayer(String playerName) {

        ChannelPlayer cp = ChannelPlayer.getChannelPlayer(playerName);
        Collection<Channel> result = new ArrayList<>();
        for (String key : channels.keySet()) {
            Channel channel = channels.get(key);
            if (channel.getMembers().contains(cp) ||
                    channel.isGlobalChannel()) {
                result.add(channel);
            }
        }
        return result;
    }

    /**
     * プレイヤーが参加しているデフォルトのチャンネルを返す
     *
     * @param playerName プレイヤー
     * @return チャンネル
     * @see com.github.ucchyocean.lc.LunaChatAPI#getDefaultChannel(java.lang.String)
     */
    @Override
    public Channel getDefaultChannel(String playerName) {

        String cname = defaultChannels.get(playerName);

        if (cname == null || !isExistChannel(cname)) {
            return null;
        }
        return channels.get(cname);
    }

    /**
     * プレイヤーのデフォルトチャンネルを設定する
     *
     * @param playerName  プレイヤー
     * @param channelName チャンネル名
     * @see com.github.ucchyocean.lc.LunaChatAPI#setDefaultChannel(java.lang.String, java.lang.String)
     */
    @Override
    public void setDefaultChannel(String playerName, String channelName) {
        if (channelName == null) {
            removeDefaultChannel(playerName);
            return;
        }
        defaultChannels.put(playerName, channelName.toLowerCase());
        saveDefaults();
    }

    /**
     * 指定した名前のプレイヤーに設定されている、デフォルトチャンネルを削除する
     *
     * @param playerName プレイヤー名
     * @see com.github.ucchyocean.lc.LunaChatAPI#removeDefaultChannel(java.lang.String)
     */
    @Override
    public void removeDefaultChannel(String playerName) {
        defaultChannels.remove(playerName);
        saveDefaults();
    }

    /**
     * チャンネルを取得する
     *
     * @param channelName チャンネル名、または、チャンネルの別名
     * @return チャンネル
     * @see com.github.ucchyocean.lc.LunaChatAPI#getChannel(java.lang.String)
     */
    @Override
    public Channel getChannel(String channelName) {
        if (channelName == null) return null;
        Channel channel = channels.get(channelName.toLowerCase());
        if (channel != null) return channel;
        for (Channel ch : channels.values()) {
            String alias = ch.getAlias();
            if (alias != null && alias.length() > 0
                    && channelName.equalsIgnoreCase(ch.getAlias())) {
                return ch;
            }
        }
        return null;
    }

    /**
     * 新しいチャンネルを作成する
     *
     * @param channelName チャンネル名
     * @return 作成されたチャンネル
     * @see com.github.ucchyocean.lc.LunaChatAPI#createChannel(java.lang.String)
     */
    @Override
    public Channel createChannel(String channelName) {
        return createChannel(channelName, null);
    }

    /**
     * 新しいチャンネルを作成する
     *
     * @param channelName チャンネル名
     * @return 作成されたチャンネル
     * @see com.github.ucchyocean.lc.LunaChatAPI#createChannel(java.lang.String, org.bukkit.command.CommandSender)
     */
    @Override
    public Channel createChannel(String channelName, CommandSender sender) {

        // イベントコール
        LunaChatChannelCreateEvent event =
                new LunaChatChannelCreateEvent(channelName, sender);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return null;
        }
        String name = event.getChannelName();

        Channel channel = new ChannelImpl(name);
        channels.put(name.toLowerCase(), channel);
        channel.save();
        return channel;
    }

    /**
     * チャンネルを削除する
     *
     * @param channelName 削除するチャンネル名
     * @return 削除したかどうか
     * @see com.github.ucchyocean.lc.LunaChatAPI#removeChannel(java.lang.String)
     */
    @Override
    public boolean removeChannel(String channelName) {
        return removeChannel(channelName, null);
    }

    /**
     * チャンネルを削除する
     *
     * @param channelName 削除するチャンネル名
     * @return 削除したかどうか
     * @see com.github.ucchyocean.lc.LunaChatAPI#removeChannel(java.lang.String, org.bukkit.command.CommandSender)
     */
    @Override
    public boolean removeChannel(String channelName, CommandSender sender) {

        channelName = channelName.toLowerCase();

        // イベントコール
        LunaChatChannelRemoveEvent event =
                new LunaChatChannelRemoveEvent(channelName, sender);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        Channel channel = getChannel(channelName);
        if (channel != null) {

            // 強制解散のメッセージを、残ったメンバーに流す
            if (!channel.isPersonalChat() && !MSG_BREAKUP.equals("")) {
                String message = MSG_BREAKUP;
                message = message.replace("%ch", channel.getName());
                message = message.replace("%color", channel.getColorCode());
                for (ChannelPlayer cp : channel.getMembers()) {
                    cp.sendMessage(message);
                }
            }

            // チャンネルの削除
            channel.remove();
            channels.remove(channelName);
        }

        return true;
    }

    /**
     * テンプレートを取得する
     *
     * @param id テンプレートID
     * @return テンプレート
     * @see com.github.ucchyocean.lc.LunaChatAPI#getTemplate(java.lang.String)
     */
    @Override
    public String getTemplate(String id) {
        return templates.get(id);
    }

    /**
     * テンプレートを登録する
     *
     * @param id       テンプレートID
     * @param template テンプレート
     * @see com.github.ucchyocean.lc.LunaChatAPI#setTemplate(java.lang.String, java.lang.String)
     */
    @Override
    public void setTemplate(String id, String template) {
        templates.put(id, template);
        saveTemplates();
    }

    /**
     * テンプレートを削除する
     *
     * @param id テンプレートID
     * @see com.github.ucchyocean.lc.LunaChatAPI#removeTemplate(java.lang.String)
     */
    @Override
    public void removeTemplate(String id) {
        templates.remove(id);
        saveTemplates();
    }

    /**
     * 辞書データを全て取得する
     *
     * @return 辞書データ
     */
    public HashMap<String, String> getAllDictionary() {
        return dictionary;
    }

    /**
     * 新しい辞書データを追加する
     *
     * @param key   キー
     * @param value 値
     */
    public void setDictionary(String key, String value) {
        dictionary.put(key, value);
        saveDictionary();
    }

    /**
     * 指定したキーの辞書データを削除する
     *
     * @param key キー
     */
    public void removeDictionary(String key) {
        dictionary.remove(key);
        saveDictionary();
    }

    /**
     * 該当のプレイヤーに関連するhidelistを取得する。
     *
     * @param key プレイヤー
     * @return 指定されたプレイヤーをhideしているプレイヤー(非null)
     */
    public List<ChannelPlayer> getHidelist(ChannelPlayer key) {
        if (key == null) {
            return new ArrayList<>();
        }
        if (hidelist.containsKey(key.toString())) {
            return hidelist.get(key.toString());
        }
        return new ArrayList<>();
    }

    /**
     * 該当のプレイヤーがhideしているプレイヤーのリストを返す。
     *
     * @param player プレイヤー
     * @return 指定したプレイヤーがhideしているプレイヤーのリスト(非null)
     */
    public ArrayList<ChannelPlayer> getHideinfo(ChannelPlayer player) {
        if (player == null) {
            return new ArrayList<>();
        }
        ArrayList<ChannelPlayer> info = new ArrayList<>();
        for (String key : hidelist.keySet()) {
            if (hidelist.get(key).contains(player)) {
                info.add(ChannelPlayer.getChannelPlayer(key));
            }
        }
        return info;
    }

    /**
     * 指定されたプレイヤーが、指定されたプレイヤーをhideするように設定する。
     *
     * @param player hideする側のプレイヤー
     * @param hided  hideされる側のプレイヤー
     */
    public void addHidelist(ChannelPlayer player, ChannelPlayer hided) {
        String hidedId = hided.toString();
        if (!hidelist.containsKey(hidedId)) {
            hidelist.put(hidedId, new ArrayList<>());
        }
        if (!hidelist.get(hidedId).contains(player)) {
            hidelist.get(hidedId).add(player);
            saveHidelist();
        }
    }

    /**
     * 指定されたプレイヤーが、指定されたプレイヤーのhideを解除するように設定する。
     *
     * @param player hideしていた側のプレイヤー
     * @param hided  hideされていた側のプレイヤー
     */
    public void removeHidelist(ChannelPlayer player, ChannelPlayer hided) {
        String hidedId = hided.toString();
        if (!hidelist.containsKey(hidedId)) {
            return;
        }
        if (hidelist.get(hidedId).contains(player)) {
            hidelist.get(hidedId).remove(player);
            if (hidelist.get(hidedId).size() <= 0) {
                hidelist.remove(hidedId);
            }
            saveHidelist();
        }
    }

    /**
     * Japanize変換を行う
     *
     * @param message 変換するメッセージ
     * @param type    変換タイプ
     * @return 変換後のメッセージ、ただしイベントでキャンセルされた場合はnullが返されるので注意
     */
    @Override
    public String japanize(String message, JapanizeType type) {

        if (type == JapanizeType.NONE) {
            return message;
        }

        // Japanize変換タスクを作成して、同期で実行する。
        DelayedJapanizeConvertTask task = new DelayedJapanizeConvertTask(
                message, type, null, null, "%japanize");
        if (task.runSync()) {
            return task.getResult();
        }
        return null;
    }

    /**
     * 該当プレイヤーのJapanize変換をオン/オフする
     *
     * @param playerName 設定するプレイヤー名
     * @param doJapanize Japanize変換するかどうか
     */
    @Override
    public void setPlayersJapanize(String playerName, boolean doJapanize) {
        japanize.put(playerName, doJapanize);
        saveJapanize();
    }

    /**
     * ChannelPlayerのリストを、IDのStringリストに変換して返す
     *
     * @param players ChannelPlayer の List
     * @return ID の String List
     */
    private List<String> getIdList(List<ChannelPlayer> players) {
        List<String> results = new ArrayList<>();
        for (ChannelPlayer cp : players) {
            results.add(cp.toString());
        }
        return results;
    }
}
