/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.lc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * ユーティリティクラス
 *
 * @author ucchy
 */
public class Utility {

    /**
     * 文字列内のカラーコード候補（&a）を、カラーコード（§a）に置き換えする
     *
     * @param source 置き換え元の文字列
     * @return 置き換え後の文字列
     */
    public static String replaceColorCode(String source) {
        if (source == null)
            return null;
        return ChatColor.translateAlternateColorCodes('&', source);
    }

    /**
     * 文字列に含まれているカラーコード（§a）を除去して返す
     *
     * @param source 置き換え元の文字列
     * @return 置き換え後の文字列
     */
    public static String stripColor(String source) {
        if (source == null)
            return null;
        return ChatColor.stripColor(source);
    }

    /**
     * 指定された文字数のアスタリスクの文字列を返す
     *
     * @param length アスタリスクの個数
     * @return 指定された文字数のアスタリスク
     */
    public static String getAstariskString(int length) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < length; i++) {
            buf.append("*");
        }
        return buf.toString();
    }

    /**
     * カラー表記の文字列を、ChatColorクラスに変換する
     *
     * @param color カラー表記の文字列
     * @return ChatColorクラス
     */
    private static ChatColor changeToChatColor(String color) {

        if (isValidColor(color)) {
            return ChatColor.valueOf(color.toUpperCase());
        }
        return ChatColor.WHITE;
    }

    /**
     * カラー表記の文字列を、カラーコードに変換する
     *
     * @param color カラー表記の文字列
     * @return カラーコード
     */
    public static String changeToColorCode(String color) {

        return "&" + changeToChatColor(color).getChar();
    }

    /**
     * ChatColorで指定可能な色かどうかを判断する
     *
     * @param color カラー表記の文字列
     * @return 指定可能かどうか
     */
    public static boolean isValidColor(String color) {

        for (ChatColor c : ChatColor.values()) {
            if (c.name().equals(color.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * カラーコードかどうかを判断する
     *
     * @param code カラー表記の文字列
     * @return 指定可能かどうか
     */
    public static boolean isValidColorCode(String code) {

        if (code == null) {
            return false;
        }
        return code.matches("&[0-9a-f]");
    }
    
    /**
     * 指定された名前のオフラインプレイヤーを取得する
     *
     * @param name プレイヤー名
     * @return オフラインプレイヤー
     */
    @SuppressWarnings("deprecation")
    public static OfflinePlayer getOfflinePlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if (!player.hasPlayedBefore() && !player.isOnline())
            return null;
        return player;
    }
}
