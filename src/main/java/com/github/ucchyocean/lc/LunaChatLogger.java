/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.lc;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

/**
 * LunaChatロガー
 *
 * @author ucchy
 */
public class LunaChatLogger {

    private final DateTimeFormatter lformat;
    private final DateTimeFormatter dformat;
    private final DateTimeFormatter logYearDateFormat;

    private final String lineSeparator;

    private Path file;
    private String dirPath;
    private final String name;

    /**
     * コンストラクタ
     *
     * @param name ログ名
     */
    public LunaChatLogger(String name) {
        lformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dformat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        logYearDateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");

        lineSeparator = System.getProperty("line.separator");

        this.name = name;
        checkDir();
    }

    /**
     * ログを出力する
     *
     * @param message ログ内容
     * @param player  発言者名
     */
    public synchronized void log(final String message, final String player) {

        checkDir();

        // 以降の処理を、発言処理の負荷軽減のため、非同期実行にする。(see issue #40.)
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (!Files.exists(file)) Files.createFile(file);
                    Files.write(file, formatLog(player, ChatColor.stripColor(message)).getBytes(), StandardOpenOption.APPEND);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(LunaChat.getInstance());
    }

    /**
     * ログファイルを読み込んで、ログデータを取得する
     *
     * @param player  プレイヤー名、フィルタしないならnullを指定すること
     * @param filter  フィルタ、フィルタしないならnullを指定すること
     * @param date    日付、今日のデータを取得するならnullを指定すること
     * @param reverse 逆順取得
     * @return ログデータ
     */
    public ArrayList<String> getLog(String player, String filter, String date, boolean reverse) {
        // 指定された日付のログを取得する
        Path f = getLogFile(date);
        if (f == null) return new ArrayList<>();

        // ログファイルの読み込み
        ArrayList<String> data = readAllLines(f);

        // プレイヤー指定なら、一致するプレイヤー名が含まれているログに絞る
        if (player != null) {
            ArrayList<String> temp = new ArrayList<>(data);
            data = new ArrayList<>();
            for (String t : temp) {
                String[] line = t.split(",");
                if (line.length >= 3 && line[2].contains(player)) {
                    data.add(t);
                }
            }
        }

        // フィルタ指定なら、指定のキーワードが含まれているログに絞る
        if (filter != null) {
            ArrayList<String> temp = new ArrayList<>(data);
            data = new ArrayList<>();
            for (String t : temp) {
                String[] line = t.split(",");
                if (line.length >= 2 && line[1].contains(filter)) data.add(t);

            }
        }

        // 逆順が指定されているなら、逆順に並び替える
        if (reverse) Collections.reverse(data);

        return data;
    }

    /**
     * テキストファイルの内容を読み出す。
     *
     * @param file ファイル
     * @return 内容
     */
    private ArrayList<String> readAllLines(Path file) {
        try {
            return new ArrayList<>(Files.readAllLines(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 指定された日付のログファイルを取得します。
     * 取得できない場合は、nullを返します。
     *
     * @param date 日付
     * @return 指定された日付のログファイル
     */
    private Path getLogFile(String date) {
        if (date == null) return file;

        LocalDate d;

        if (date.matches("[0-9]{4}")) date = LocalDate.now().getYear() + date;


        if (date.matches("[0-9]{8}")) d = LocalDate.parse(date, logYearDateFormat);
        else return null;


        Path folder = Paths.get(getFolderPath(d));
        if (!Files.isDirectory(folder) || !Files.exists(folder)) return null;

        Path f = folder.resolve(name + ".log");
        if (Files.exists(folder.resolve(name + ".log"))) return null;

        return f;
    }

    /**
     * ログの出力先フォルダをチェックし、変更されるなら更新する。
     */
    private void checkDir() {

        String temp = getFolderPath(LocalDate.now());
        if (temp.equals(dirPath)) return;

        dirPath = temp;

        Path dir = Paths.get(dirPath);
        if (!Files.isDirectory(dir) || !Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        file = dir.resolve(name + ".log");
    }

    /**
     * 指定された日付のログファイル名を生成して返します。
     *
     * @param date 日付
     * @return ログファイル名
     */
    private String getFolderPath(LocalDate date) {
        return LunaChat.getInstance().getDataFolder() + FileSystems.getDefault().getSeparator() +
                "logs" + FileSystems.getDefault().getSeparator() + dformat.format(date);
    }

    private String formatLog(String player, String msg) {
        return lformat.format(LocalDateTime.now()) + "," + msg + "," + player + lineSeparator;
    }
}
