package com.github.ucchyocean.lc3.channel;

public enum ChatSource {

    DYNMAP("web"),
    DISCORD("discord"),
    OTHER("other");

    private final String source;

    /**
     * 値を指定して enum 定数を生成。
     */
    ChatSource(String source) {
        this.source = source;
    }

    /**
     * 値を返すメソッドを用意。
     */
    public String getSource() {
        return source;
    }

    /**
     * 値に合致する enum 定数を返す。
     */
    public static ChatSource getByString(String source) {
        // 値から enum 定数を特定して返す処理
        for (ChatSource value : ChatSource.values()) {
            if (value.getSource().equals(source) ) {
                return value;
            }
        }
        return OTHER; // 特定できない場合
    }
}
