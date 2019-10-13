/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.lc.command;

import java.util.HashMap;

/**
 * データマップ
 * @author ucchy
 */
class DataMaps {

    /** 招待された人→招待されたチャンネル名 のマップ */
    static final HashMap<String, String> inviteMap;

    /** 招待された人→招待した人 のマップ */
    static final HashMap<String, String> inviterMap;

    /** tell/rコマンドの送信者→受信者 のマップ */
    static final HashMap<String, String> privateMessageMap;

    static {
        inviteMap = new HashMap<>();
        inviterMap = new HashMap<>();
        privateMessageMap = new HashMap<>();
    }
}
